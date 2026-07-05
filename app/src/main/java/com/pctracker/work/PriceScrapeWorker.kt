package com.pctracker.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pctracker.data.Provider
import com.pctracker.data.db.AppDatabase
import com.pctracker.data.repository.PriceRepository
import com.pctracker.domain.BestConfigResult
import com.pctracker.notification.NotificationHelper
import com.pctracker.scraper.HttpFetcher
import com.pctracker.scraper.ScraperFactory
import kotlinx.coroutines.flow.first

class PriceScrapeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val repo = PriceRepository.getInstance(db)
            repo.ensureSeeded()

            val links = repo.getEnabledProductLinks()
            if (links.isEmpty()) return Result.success()

            val results = mutableListOf<PriceRepository.ScrapedResult>()
            for (link in links) {
                val provider = runCatching { Provider.valueOf(link.provider) }.getOrNull() ?: continue
                val fetched = HttpFetcher.fetch(link.url)
                val scraped = fetched.fold(
                    onSuccess = { html ->
                        val parsed = ScraperFactory.forProvider(provider).parse(html, link.url)
                        PriceRepository.ScrapedResult(
                            componentId = link.componentId,
                            productLinkId = link.id,
                            provider = provider,
                            price = parsed.price,
                            available = parsed.available,
                            title = parsed.title,
                            productUrl = link.url,
                            error = parsed.error
                        )
                    },
                    onFailure = { throwable ->
                        PriceRepository.ScrapedResult(
                            componentId = link.componentId,
                            productLinkId = link.id,
                            provider = provider,
                            price = null,
                            available = false,
                            title = null,
                            productUrl = link.url,
                            error = throwable.message ?: "Erreur reseau"
                        )
                    }
                )
                results += scraped
            }

            val now = System.currentTimeMillis()
            val bestConfig = repo.recordScrapeCycle(results, now)
            repo.pruneOldData(now)
            maybeNotify(repo, bestConfig, now)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun maybeNotify(repo: PriceRepository, bestConfig: BestConfigResult, now: Long) {
        val settings = repo.appSettings.first() ?: return
        val threshold = settings.thresholdTotal ?: return
        // Wait until every component has at least one price before alerting, otherwise an
        // incomplete config could look artificially cheap.
        if (bestConfig.missingComponents.isNotEmpty()) return

        val isBelowThreshold = bestConfig.total < threshold
        if (isBelowThreshold && !settings.wasBelowThreshold) {
            NotificationHelper.ensureChannel(applicationContext)
            NotificationHelper.showBestConfigNotification(applicationContext, bestConfig)
        }
        repo.markNotified(bestConfig.total, now, isBelowThreshold)
    }
}
