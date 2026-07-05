package com.pctracker.scraper

data class ParsedProduct(
    val price: Double?,
    val available: Boolean,
    val title: String?,
    val error: String? = null
)

interface ProviderScraper {
    fun parse(html: String, url: String): ParsedProduct
}
