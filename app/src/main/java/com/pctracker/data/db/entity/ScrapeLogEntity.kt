package com.pctracker.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local log of every scrape attempt, kept for troubleshooting when a site's HTML structure
 * changes or a request gets blocked. Trimmed periodically to avoid unbounded growth.
 */
@Entity(tableName = "scrape_logs", indices = [Index("timestamp")])
data class ScrapeLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val provider: String,
    val componentId: Long?,
    val productUrl: String?,
    val success: Boolean,
    val message: String?
)
