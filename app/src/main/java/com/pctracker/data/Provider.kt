package com.pctracker.data

/**
 * The five sources tracked by the app. Fnac/Darty/Boulanger are grouped into a single
 * provider because they share the same CSE (Bons Plans) discount and gift voucher pool.
 */
enum class Provider(val displayName: String, val domainHints: List<String>) {
    LDLC("LDLC", listOf("ldlc.com")),
    MATERIEL_NET("Materiel.net", listOf("materiel.net")),
    TOPACHAT("Topachat", listOf("topachat.com")),
    FNAC_DARTY("Fnac / Darty / Boulanger", listOf("fnac.com", "darty.com", "boulanger.com")),
    AMAZON("Amazon", listOf("amazon.fr"));

    companion object {
        fun fromUrl(url: String): Provider? {
            val lower = url.lowercase()
            return entries.firstOrNull { provider -> provider.domainHints.any { lower.contains(it) } }
        }
    }
}
