package com.pctracker.scraper

class LdlcScraper : BaseProviderScraper(
    priceSelectors = listOf(
        "meta[itemprop=price]",
        "div.price .price",
        "div.price",
        "span.price",
        "[data-price]"
    ),
    availabilitySelectors = listOf(
        ".stock-web",
        ".product-stock",
        ".availability"
    )
)
