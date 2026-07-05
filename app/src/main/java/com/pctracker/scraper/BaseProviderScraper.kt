package com.pctracker.scraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Shared parsing pipeline for every provider: structured data (JSON-LD / microdata) first,
 * since it survives visual redesigns; then a list of site-specific CSS selectors as a
 * fallback. Subclasses only need to supply those selector lists - update them here if a
 * site changes its markup and structured data stops being present.
 */
abstract class BaseProviderScraper(
    private val priceSelectors: List<String>,
    private val availabilitySelectors: List<String>,
    private val outOfStockKeywords: List<String> = DEFAULT_OUT_OF_STOCK_KEYWORDS
) : ProviderScraper {

    override fun parse(html: String, url: String): ParsedProduct {
        return try {
            val doc = Jsoup.parse(html, url)
            val title = StructuredDataExtractor.extractTitle(doc)
            val price = StructuredDataExtractor.extractPrice(doc) ?: findPriceViaSelectors(doc)
            val available = StructuredDataExtractor.extractAvailability(doc)
                ?: inferAvailabilityFromText(doc)

            if (price == null) {
                ParsedProduct(price = null, available = available, title = title, error = "Prix introuvable (selecteurs a verifier)")
            } else {
                ParsedProduct(price = price, available = available, title = title, error = null)
            }
        } catch (e: Exception) {
            ParsedProduct(price = null, available = false, title = null, error = e.message ?: "Erreur de parsing")
        }
    }

    private fun findPriceViaSelectors(doc: Document): Double? {
        for (selector in priceSelectors) {
            val element = doc.selectFirst(selector) ?: continue
            val text = element.attr("content").ifBlank { element.text() }
            PriceParsingUtils.parsePrice(text)?.let { return it }
        }
        return null
    }

    private fun inferAvailabilityFromText(doc: Document): Boolean {
        for (selector in availabilitySelectors) {
            val text = doc.selectFirst(selector)?.text()?.lowercase() ?: continue
            if (outOfStockKeywords.any { text.contains(it) }) return false
        }
        val bodyText = doc.body()?.text()?.lowercase().orEmpty()
        return outOfStockKeywords.none { bodyText.contains(it) }
    }

    companion object {
        val DEFAULT_OUT_OF_STOCK_KEYWORDS = listOf(
            "rupture de stock", "indisponible", "epuise", "out of stock", "hors stock", "non disponible"
        )
    }
}
