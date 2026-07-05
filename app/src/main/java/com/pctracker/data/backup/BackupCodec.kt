package com.pctracker.data.backup

import org.json.JSONArray
import org.json.JSONObject

/**
 * Product links reference components by their stable [key] (e.g. "gpu"), not their
 * autoincrement id, so a backup survives a reinstall where ids are reassigned.
 */
data class ProductLinkBackup(
    val componentKey: String,
    val provider: String,
    val url: String,
    val label: String?,
    val enabled: Boolean
)

data class BackupPayload(
    val productLinks: List<ProductLinkBackup>,
    val providerDiscounts: Map<String, Double>,
    val voucherBalance: Double,
    val voucherInitialAmount: Double,
    val voucherExpirationDate: Long?,
    val thresholdTotal: Double?,
    val autoThresholdEnabled: Boolean,
    val scrapeIntervalHours: Int
)

object BackupCodec {
    private const val FORMAT_VERSION = 1

    fun encode(payload: BackupPayload): String {
        val root = JSONObject()
        root.put("formatVersion", FORMAT_VERSION)

        val links = JSONArray()
        payload.productLinks.forEach { link ->
            links.put(
                JSONObject().apply {
                    put("componentKey", link.componentKey)
                    put("provider", link.provider)
                    put("url", link.url)
                    put("label", link.label)
                    put("enabled", link.enabled)
                }
            )
        }
        root.put("productLinks", links)

        val discounts = JSONObject()
        payload.providerDiscounts.forEach { (provider, percent) -> discounts.put(provider, percent) }
        root.put("providerDiscounts", discounts)

        root.put("voucherBalance", payload.voucherBalance)
        root.put("voucherInitialAmount", payload.voucherInitialAmount)
        root.put("voucherExpirationDate", payload.voucherExpirationDate)
        root.put("thresholdTotal", payload.thresholdTotal)
        root.put("autoThresholdEnabled", payload.autoThresholdEnabled)
        root.put("scrapeIntervalHours", payload.scrapeIntervalHours)

        return root.toString(2)
    }

    fun decode(json: String): BackupPayload {
        val root = JSONObject(json)

        val links = mutableListOf<ProductLinkBackup>()
        val linksArray = root.optJSONArray("productLinks") ?: JSONArray()
        for (i in 0 until linksArray.length()) {
            val obj = linksArray.getJSONObject(i)
            links += ProductLinkBackup(
                componentKey = obj.getString("componentKey"),
                provider = obj.getString("provider"),
                url = obj.getString("url"),
                label = if (obj.isNull("label")) null else obj.optString("label"),
                enabled = obj.optBoolean("enabled", true)
            )
        }

        val discounts = mutableMapOf<String, Double>()
        val discountsObj = root.optJSONObject("providerDiscounts") ?: JSONObject()
        discountsObj.keys().forEach { key -> discounts[key] = discountsObj.getDouble(key) }

        return BackupPayload(
            productLinks = links,
            providerDiscounts = discounts,
            voucherBalance = root.optDouble("voucherBalance", 0.0),
            voucherInitialAmount = root.optDouble("voucherInitialAmount", 0.0),
            voucherExpirationDate = if (root.isNull("voucherExpirationDate")) null else root.optLong("voucherExpirationDate"),
            thresholdTotal = if (root.isNull("thresholdTotal")) null else root.optDouble("thresholdTotal"),
            autoThresholdEnabled = root.optBoolean("autoThresholdEnabled", true),
            scrapeIntervalHours = root.optInt("scrapeIntervalHours", 3)
        )
    }
}
