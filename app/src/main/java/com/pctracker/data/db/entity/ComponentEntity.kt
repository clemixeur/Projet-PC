package com.pctracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One slot in the target build (CPU, GPU, RAM, ...). [key] is a stable identifier used by
 * seed data and never shown to the user; [referencePrice] is the catalog price used only
 * as a fallback when computing the initial auto-threshold before any history exists.
 */
@Entity(tableName = "components")
data class ComponentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val displayName: String,
    val category: String,
    val referencePrice: Double,
    val position: Int
)
