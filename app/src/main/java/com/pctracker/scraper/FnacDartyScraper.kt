package com.pctracker.scraper

/**
 * Covers fnac.com, darty.com and boulanger.com - they are commercially and technically
 * related (Fnac Darty group) and expose broadly similar product page markup.
 */
class FnacDartyScraper : BaseProviderScraper(
    priceSelectors = listOf(
        "meta[itemprop=price]",
        ".f-priceBox__price",
        ".userPrice",
        ".product-price",
        "div.price"
    ),
    availabilitySelectors = listOf(
        ".f-productAvailability",
        ".availability",
        ".stock"
    )
)
