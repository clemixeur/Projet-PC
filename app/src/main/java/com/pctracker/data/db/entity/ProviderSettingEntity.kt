package com.pctracker.data.db.entity

import androidx.room.Entity

/**
 * Per-provider discount percentage (cashback or CSE), editable in Settings.
 * [provider] holds the Provider enum name and is the primary key (one row per provider).
 */
@Entity(tableName = "provider_settings", primaryKeys = ["provider"])
data class ProviderSettingEntity(
    val provider: String,
    val discountPercent: Double,
    val note: String
)
