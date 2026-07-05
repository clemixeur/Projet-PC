package com.pctracker.data.repository

import com.pctracker.data.Provider
import com.pctracker.data.db.AppDatabase
import com.pctracker.data.db.entity.AppSettingsEntity
import com.pctracker.data.db.entity.ComponentEntity
import com.pctracker.data.db.entity.PriceSnapshotEntity
import com.pctracker.data.db.entity.ProductLinkEntity
import com.pctracker.data.db.entity.ProviderSettingEntity
import com.pctracker.data.db.entity.ScrapeLogEntity
import com.pctracker.data.db.entity.TotalHistoryEntity
import com.pctracker.data.db.entity.VoucherEntity
import com.pctracker.data.seed.SeedData
import com.pctracker.domain.BestConfigResult
import com.pctracker.domain.ComponentOffer
import com.pctracker.domain.DiscountCalculator
import com.pctracker.domain.ThresholdCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class PriceRepository(private val db: AppDatabase) {

    val components: Flow<List<ComponentEntity>> = db.componentDao().observeAll()
    val productLinks: Flow<List<ProductLinkEntity>> = db.productLinkDao().observeAll()
    val latestSnapshots: Flow<List<PriceSnapshotEntity>> = db.priceSnapshotDao().observeLatestPerComponentAndProvider()
    val providerSettings: Flow<List<ProviderSettingEntity>> = db.providerSettingDao().observeAll()
    val voucher: Flow<VoucherEntity?> = db.voucherDao().observe()
    val appSettings: Flow<AppSettingsEntity?> = db.appSettingsDao().observe()
    val totalHistory: Flow<List<TotalHistoryEntity>> = db.totalHistoryDao().observeAll()

    val bestConfig: Flow<BestConfigResult> = combine(
        components, latestSnapshots, providerSettings, voucher
    ) { comps, snapshots, discounts, voucherEntity ->
        computeBestConfig(comps, snapshots, discounts, voucherEntity?.balance ?: 0.0)
    }

    suspend fun ensureSeeded() {
        if (db.componentDao().count() == 0) db.componentDao().insertAll(SeedData.components)
        if (db.providerSettingDao().count() == 0) db.providerSettingDao().insertAll(SeedData.providerSettings)
        if (db.voucherDao().get() == null) db.voucherDao().insert(SeedData.voucher)
        if (db.appSettingsDao().get() == null) db.appSettingsDao().insert(SeedData.appSettings)
    }

    private fun computeBestConfig(
        comps: List<ComponentEntity>,
        snapshots: List<PriceSnapshotEntity>,
        discounts: List<ProviderSettingEntity>,
        voucherBalance: Double
    ): BestConfigResult {
        val discountByProvider = discounts.associate { it.provider to it.discountPercent }
        val offersByComponent = mutableMapOf<Long, MutableList<ComponentOffer>>()
        for (snapshot in snapshots) {
            val raw = snapshot.rawPrice ?: continue
            if (!snapshot.available) continue
            val provider = runCatching { Provider.valueOf(snapshot.provider) }.getOrNull() ?: continue
            val discountPercent = discountByProvider[snapshot.provider] ?: 0.0
            val net = raw * (1 - discountPercent / 100.0)
            offersByComponent.getOrPut(snapshot.componentId) { mutableListOf() } += ComponentOffer(
                provider = provider,
                rawPrice = raw,
                netPrice = net,
                productUrl = snapshot.productUrl,
                title = snapshot.title,
                timestamp = snapshot.timestamp
            )
        }
        return DiscountCalculator.compute(comps, offersByComponent, voucherBalance)
    }

    /** Called by the worker after each scrape cycle: persists results, updates the total
     * history point, refreshes the auto-threshold if applicable, and returns the fresh
     * best-config so the worker can decide whether to notify. */
    suspend fun recordScrapeCycle(results: List<ScrapedResult>, now: Long): BestConfigResult {
        val snapshotEntities = results.map {
            PriceSnapshotEntity(
                componentId = it.componentId,
                provider = it.provider.name,
                productLinkId = it.productLinkId,
                rawPrice = it.price,
                available = it.available,
                title = it.title,
                productUrl = it.productUrl,
                timestamp = now,
                error = it.error
            )
        }
        db.priceSnapshotDao().insertAll(snapshotEntities)

        for (result in results) {
            db.scrapeLogDao().insert(
                ScrapeLogEntity(
                    timestamp = now,
                    provider = result.provider.name,
                    componentId = result.componentId,
                    success = result.error == null,
                    message = result.error ?: "OK"
                )
            )
        }

        val comps = db.componentDao().getAll()
        val latest = db.priceSnapshotDao().observeLatestPerComponentAndProvider().first()
        val discounts = db.providerSettingDao().getAll()
        val voucherEntity = db.voucherDao().get()
        val config = computeBestConfig(comps, latest, discounts, voucherEntity?.balance ?: 0.0)

        db.totalHistoryDao().insert(TotalHistoryEntity(timestamp = now, total = config.total))
        refreshAutoThresholdIfNeeded(now)

        return config
    }

    private suspend fun refreshAutoThresholdIfNeeded(now: Long) {
        val settings = db.appSettingsDao().get() ?: return
        if (!settings.autoThresholdEnabled) return
        val firstTimestamp = db.totalHistoryDao().getFirstTimestamp() ?: return

        // computeAutoThreshold returns null until the first 7 days of history are complete,
        // so calling this every cycle is harmless - it just keeps returning null until then,
        // and afterwards keeps recomputing the same fixed-window average (idempotent).
        val windowEnd = firstTimestamp + java.util.concurrent.TimeUnit.DAYS.toMillis(7)
        val entries = db.totalHistoryDao().getBetween(firstTimestamp, windowEnd)
        val autoThreshold = ThresholdCalculator.computeAutoThreshold(
            firstTrackedAtMillis = firstTimestamp,
            dailyTotalsInWindow = entries.map { it.total },
            nowMillis = now
        )
        if (autoThreshold != null) {
            db.appSettingsDao().update(settings.copy(thresholdTotal = autoThreshold))
        }
    }

    suspend fun updateThreshold(value: Double) {
        val settings = db.appSettingsDao().get() ?: SeedData.appSettings
        db.appSettingsDao().update(settings.copy(thresholdTotal = value, autoThresholdEnabled = false))
    }

    suspend fun setAutoThresholdEnabled(enabled: Boolean) {
        val settings = db.appSettingsDao().get() ?: SeedData.appSettings
        db.appSettingsDao().update(settings.copy(autoThresholdEnabled = enabled))
    }

    suspend fun setScrapeIntervalHours(hours: Int) {
        val settings = db.appSettingsDao().get() ?: SeedData.appSettings
        db.appSettingsDao().update(settings.copy(scrapeIntervalHours = hours))
    }

    suspend fun markNotified(total: Double, now: Long, below: Boolean) {
        val settings = db.appSettingsDao().get() ?: return
        db.appSettingsDao().update(
            settings.copy(lastNotifiedTotal = total, lastNotificationTimestamp = now, wasBelowThreshold = below)
        )
    }

    suspend fun updateProviderDiscount(provider: Provider, percent: Double) {
        val current = db.providerSettingDao().getAll().firstOrNull { it.provider == provider.name }
            ?: ProviderSettingEntity(provider.name, percent, "")
        db.providerSettingDao().update(current.copy(discountPercent = percent))
    }

    suspend fun updateVoucher(balance: Double, expirationDate: Long?) {
        val current = db.voucherDao().get() ?: SeedData.voucher
        db.voucherDao().update(current.copy(balance = balance, expirationDate = expirationDate))
    }

    suspend fun addProductLink(componentId: Long, provider: Provider, url: String, label: String?) {
        db.productLinkDao().insert(ProductLinkEntity(componentId = componentId, provider = provider.name, url = url, label = label))
    }

    suspend fun updateProductLink(link: ProductLinkEntity) {
        db.productLinkDao().update(link)
    }

    suspend fun deleteProductLink(link: ProductLinkEntity) {
        db.productLinkDao().delete(link)
    }

    suspend fun getEnabledProductLinks(): List<ProductLinkEntity> = db.productLinkDao().getAllEnabled()

    fun historyForComponent(componentId: Long): Flow<List<PriceSnapshotEntity>> =
        db.priceSnapshotDao().observeHistoryForComponent(componentId)

    fun productLinksForComponent(componentId: Long): Flow<List<ProductLinkEntity>> =
        db.productLinkDao().observeForComponent(componentId)

    suspend fun pruneOldData(now: Long) {
        val cutoff = now - java.util.concurrent.TimeUnit.DAYS.toMillis(180)
        db.priceSnapshotDao().deleteOlderThan(cutoff)
        db.scrapeLogDao().deleteOlderThan(cutoff)
    }

    data class ScrapedResult(
        val componentId: Long,
        val productLinkId: Long,
        val provider: Provider,
        val price: Double?,
        val available: Boolean,
        val title: String?,
        val productUrl: String,
        val error: String?
    )

    companion object {
        @Volatile
        private var INSTANCE: PriceRepository? = null

        fun getInstance(db: AppDatabase): PriceRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PriceRepository(db).also { INSTANCE = it }
            }
        }
    }
}
