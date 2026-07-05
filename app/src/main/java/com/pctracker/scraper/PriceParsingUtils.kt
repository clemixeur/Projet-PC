package com.pctracker.scraper

object PriceParsingUtils {
    // Matches amounts like "1234,56", "629,99", "1234.56" or plain "630" once whitespace
    // (including the non-breaking spaces French sites use as thousand separators) is stripped.
    private val priceRegex = Regex("(\\d[\\d.,]*)")
    private const val NBSP = ' '
    private const val NARROW_NBSP = ' '

    fun parsePrice(raw: String?): Double? {
        if (raw.isNullOrBlank()) return null
        val cleaned = raw
            .filterNot { it.isWhitespace() || it == NBSP || it == NARROW_NBSP }
            .trim()
        val match = priceRegex.find(cleaned) ?: return null
        var numberPart = match.groupValues[1]
        if (numberPart.isEmpty()) return null

        val lastComma = numberPart.lastIndexOf(',')
        val lastDot = numberPart.lastIndexOf('.')
        numberPart = when {
            lastComma > lastDot -> numberPart.replace(".", "").replace(",", ".")
            lastDot > lastComma -> numberPart.replace(",", "")
            else -> numberPart
        }
        return numberPart.toDoubleOrNull()
    }
}
