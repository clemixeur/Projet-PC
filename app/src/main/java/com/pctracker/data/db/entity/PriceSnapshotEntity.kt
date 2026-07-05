package com.pctracker.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Result of one scrape attempt for one product link. Kept even on failure (rawPrice null,
 * error set) so the history and scrape logs stay consistent.
 */
@Entity(
    tableName = "price_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = ComponentEntity::class,
            parentColumns = ["id"],
            childColumns = ["componentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("componentId"), Index("provider"), Index("timestamp")]
)
data class PriceSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val componentId: Long,
    val provider: String,
    val productLinkId: Long,
    val rawPrice: Double?,
    val available: Boolean,
    val title: String?,
    val productUrl: String,
    val timestamp: Long,
    val error: String? = null
)
