package com.pctracker.scraper

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.jsoup.nodes.Document

/**
 * Most French e-commerce sites embed schema.org Product/Offer data (JSON-LD or microdata)
 * for SEO regardless of how their visible page markup is redesigned. Trying this first,
 * before any site-specific CSS selector, makes each provider scraper far less likely to
 * break the next time a site changes its layout.
 */
object StructuredDataExtractor {

    fun extractPrice(doc: Document): Double? {
        for (script in doc.select("script[type=application/ld+json]")) {
            findFirstNumeric(parseJsonSafely(script.data()), PRICE_KEYS)?.let { return it }
        }
        doc.selectFirst("meta[itemprop=price]")?.attr("content")?.let {
            PriceParsingUtils.parsePrice(it)?.let { price -> return price }
        }
        doc.selectFirst("meta[property=product:price:amount]")?.attr("content")?.let {
            PriceParsingUtils.parsePrice(it)?.let { price -> return price }
        }
        doc.selectFirst("[itemprop=price]")?.let { el ->
            val content = el.attr("content").ifBlank { el.text() }
            PriceParsingUtils.parsePrice(content)?.let { return it }
        }
        return null
    }

    fun extractAvailability(doc: Document): Boolean? {
        for (script in doc.select("script[type=application/ld+json]")) {
            findFirstString(parseJsonSafely(script.data()), AVAILABILITY_KEYS)?.let {
                return it.contains("InStock", ignoreCase = true) && !it.contains("OutOfStock", ignoreCase = true)
            }
        }
        doc.selectFirst("meta[property=product:availability]")?.attr("content")?.let {
            return it.contains("in stock", ignoreCase = true) || it.contains("instock", ignoreCase = true)
        }
        doc.selectFirst("[itemprop=availability]")?.let { el ->
            val href = el.attr("href").ifBlank { el.attr("content") }
            return href.contains("InStock", ignoreCase = true) && !href.contains("OutOfStock", ignoreCase = true)
        }
        return null
    }

    fun extractTitle(doc: Document): String? {
        doc.selectFirst("meta[property=og:title]")?.attr("content")?.takeIf { it.isNotBlank() }?.let { return it }
        doc.selectFirst("h1")?.text()?.takeIf { it.isNotBlank() }?.let { return it }
        return doc.title().takeIf { it.isNotBlank() }
    }

    private fun parseJsonSafely(raw: String): Any? = runCatching { JSONTokener(raw).nextValue() }.getOrNull()

    private fun findFirstNumeric(node: Any?, keys: Set<String>): Double? {
        when (node) {
            is JSONObject -> {
                for (key in keys) {
                    if (node.has(key) && !node.isNull(key)) {
                        val value = node.opt(key)
                        val parsed = when (value) {
                            is Number -> value.toDouble()
                            is String -> PriceParsingUtils.parsePrice(value)
                            else -> null
                        }
                        if (parsed != null) return parsed
                    }
                }
                val keysIterator = node.keys()
                while (keysIterator.hasNext()) {
                    findFirstNumeric(node.opt(keysIterator.next()), keys)?.let { return it }
                }
            }
            is JSONArray -> {
                for (i in 0 until node.length()) {
                    findFirstNumeric(node.opt(i), keys)?.let { return it }
                }
            }
        }
        return null
    }

    private fun findFirstString(node: Any?, keys: Set<String>): String? {
        when (node) {
            is JSONObject -> {
                for (key in keys) {
                    if (node.has(key) && !node.isNull(key)) {
                        val value = node.optString(key, "")
                        if (value.isNotBlank()) return value
                    }
                }
                val keysIterator = node.keys()
                while (keysIterator.hasNext()) {
                    findFirstString(node.opt(keysIterator.next()), keys)?.let { return it }
                }
            }
            is JSONArray -> {
                for (i in 0 until node.length()) {
                    findFirstString(node.opt(i), keys)?.let { return it }
                }
            }
        }
        return null
    }

    private val PRICE_KEYS = setOf("price", "lowPrice", "highPrice")
    private val AVAILABILITY_KEYS = setOf("availability")
}
