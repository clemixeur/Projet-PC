package com.pctracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table for global app settings (alert threshold, scrape interval, and the
 * bookkeeping needed to throttle notifications to one per newly-crossed threshold).
 */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val thresholdTotal: Double?,
    val autoThresholdEnabled: Boolean = true,
    val scrapeIntervalHours: Int = 3,
    val lastNotifiedTotal: Double? = null,
    val lastNotificationTimestamp: Long? = null,
    val wasBelowThreshold: Boolean = false
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
