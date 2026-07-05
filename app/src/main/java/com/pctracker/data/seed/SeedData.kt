package com.pctracker.data.seed

import com.pctracker.data.Provider
import com.pctracker.data.db.entity.AppSettingsEntity
import com.pctracker.data.db.entity.ComponentEntity
import com.pctracker.data.db.entity.ProviderSettingEntity
import com.pctracker.data.db.entity.VoucherEntity

/**
 * Default target build and discount rates, taken from the user's own research (see
 * conversation context: config cible ~1500-1540EUR, remises par fournisseur). Everything
 * here is editable afterwards in the app; these are just sane starting points so the app
 * is useful on first launch instead of an empty shell.
 */
object SeedData {

    val components = listOf(
        ComponentEntity(key = "cpu", displayName = "CPU - AMD Ryzen 7 7700X", category = "Processeur", referencePrice = 300.0, position = 0),
        ComponentEntity(key = "motherboard", displayName = "Carte mere - MSI PRO B650-P WiFi", category = "Carte mere", referencePrice = 150.0, position = 1),
        ComponentEntity(key = "ram", displayName = "RAM - 32 Go DDR5 6000MHz CL30 (2x16)", category = "Memoire", referencePrice = 140.0, position = 2),
        ComponentEntity(key = "gpu", displayName = "GPU - RTX 5070 12 Go", category = "Carte graphique", referencePrice = 630.0, position = 3),
        ComponentEntity(key = "ssd", displayName = "SSD NVMe Gen4 1 To", category = "Stockage", referencePrice = 75.0, position = 4),
        ComponentEntity(key = "psu", displayName = "Alimentation - Corsair RM750e", category = "Alimentation", referencePrice = 95.0, position = 5),
        ComponentEntity(key = "case", displayName = "Boitier - Fractal Pop Air / NZXT H5 Flow", category = "Boitier", referencePrice = 85.0, position = 6),
        ComponentEntity(key = "cooler", displayName = "Ventirad - Thermalright Peerless Assassin 120 SE", category = "Refroidissement", referencePrice = 38.0, position = 7)
    )

    val providerSettings = listOf(
        ProviderSettingEntity(Provider.LDLC.name, 3.0, "Cashback (iGraal/Poulpeo)"),
        ProviderSettingEntity(Provider.MATERIEL_NET.name, 3.0, "Cashback (iGraal/Poulpeo)"),
        ProviderSettingEntity(Provider.TOPACHAT.name, 0.0, "Aucune remise identifiee"),
        ProviderSettingEntity(Provider.FNAC_DARTY.name, 7.0, "CSE Swile (5-10%), non cumulable avec le bon d'achat classique"),
        ProviderSettingEntity(Provider.AMAZON.name, 3.0, "Cashback (iGraal/Poulpeo)")
    )

    val voucher = VoucherEntity(balance = 130.0, initialAmount = 130.0, expirationDate = null)

    fun defaultThreshold(): Double = components.sumOf { it.referencePrice } * 0.92

    val appSettings = AppSettingsEntity(
        thresholdTotal = defaultThreshold(),
        autoThresholdEnabled = true,
        scrapeIntervalHours = 3
    )
}
