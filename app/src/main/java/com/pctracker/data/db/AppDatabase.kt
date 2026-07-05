package com.pctracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pctracker.data.db.dao.AppSettingsDao
import com.pctracker.data.db.dao.ComponentDao
import com.pctracker.data.db.dao.PriceSnapshotDao
import com.pctracker.data.db.dao.ProductLinkDao
import com.pctracker.data.db.dao.ProviderSettingDao
import com.pctracker.data.db.dao.ScrapeLogDao
import com.pctracker.data.db.dao.TotalHistoryDao
import com.pctracker.data.db.dao.VoucherDao
import com.pctracker.data.db.entity.AppSettingsEntity
import com.pctracker.data.db.entity.ComponentEntity
import com.pctracker.data.db.entity.PriceSnapshotEntity
import com.pctracker.data.db.entity.ProductLinkEntity
import com.pctracker.data.db.entity.ProviderSettingEntity
import com.pctracker.data.db.entity.ScrapeLogEntity
import com.pctracker.data.db.entity.TotalHistoryEntity
import com.pctracker.data.db.entity.VoucherEntity

@Database(
    entities = [
        ComponentEntity::class,
        ProductLinkEntity::class,
        PriceSnapshotEntity::class,
        ProviderSettingEntity::class,
        VoucherEntity::class,
        AppSettingsEntity::class,
        ScrapeLogEntity::class,
        TotalHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun componentDao(): ComponentDao
    abstract fun productLinkDao(): ProductLinkDao
    abstract fun priceSnapshotDao(): PriceSnapshotDao
    abstract fun providerSettingDao(): ProviderSettingDao
    abstract fun voucherDao(): VoucherDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun scrapeLogDao(): ScrapeLogDao
    abstract fun totalHistoryDao(): TotalHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pctracker.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
