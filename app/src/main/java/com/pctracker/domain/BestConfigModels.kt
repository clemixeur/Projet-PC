package com.pctracker.domain

import com.pctracker.data.Provider
import com.pctracker.data.db.entity.ComponentEntity

data class ComponentOffer(
    val provider: Provider,
    val rawPrice: Double,
    val netPrice: Double,
    val productUrl: String,
    val title: String?,
    val timestamp: Long
)

data class ComponentBestPrice(
    val component: ComponentEntity,
    val offers: List<ComponentOffer>,
    val chosenProvider: Provider?,
    val chosenNetPrice: Double?,
    val chosenUrl: String?,
    val routedForVoucher: Boolean
)

data class BestConfigResult(
    val perComponent: List<ComponentBestPrice>,
    val subtotalBeforeVoucher: Double,
    val voucherApplied: Double,
    val total: Double,
    val missingComponents: List<ComponentEntity>
)
