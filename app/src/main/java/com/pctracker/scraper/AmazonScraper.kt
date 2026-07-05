package com.pctracker.scraper

class AmazonScraper : BaseProviderScraper(
    priceSelectors = listOf(
        "#corePrice_feature_div .a-price .a-offscreen",
        ".a-price .a-offscreen",
        "#priceblock_ourprice",
        "#priceblock_dealprice",
        "#tp_price_block_total_price_ww .a-offscreen"
    ),
    availabilitySelectors = listOf(
        "#availability span",
        "#availability"
    ),
    outOfStockKeywords = listOf(
        "actuellement indisponible", "temporairement en rupture", "out of stock", "unavailable"
    )
)
