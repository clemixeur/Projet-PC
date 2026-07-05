package com.pctracker.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per completed scrape cycle: the best-config total (after discounts and voucher)
 * at that point in time. Used both for the mini history chart and to compute the
 * auto-threshold (average over the first 7 days of tracking).
 */
@Entity(tableName = "total_history", indices = [Index("timestamp")])
data class TotalHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val total: Double
)
