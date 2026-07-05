package com.pctracker.scraper

class MaterielNetScraper : BaseProviderScraper(
    priceSelectors = listOf(
        "meta[itemprop=price]",
        ".o-product__price",
        ".sc-product-price",
        "div.price",
        "span.price"
    ),
    availabilitySelectors = listOf(
        ".o-product__stock",
        ".stock-web",
        ".availability"
    )
)
