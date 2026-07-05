package com.pctracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table tracking the fixed-amount Fnac/Darty/Boulanger gift voucher
 * ("Noël des salariés"). [balance] is decremented as it gets allocated to simulated or
 * real purchases; [expirationDate] (epoch millis, nullable) drives the expiry warning.
 */
@Entity(tableName = "voucher")
data class VoucherEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val balance: Double,
    val initialAmount: Double,
    val expirationDate: Long?
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
