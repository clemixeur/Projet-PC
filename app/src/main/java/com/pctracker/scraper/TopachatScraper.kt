package com.pctracker.scraper

class TopachatScraper : BaseProviderScraper(
    priceSelectors = listOf(
        "meta[itemprop=price]",
        "#prixttc",
        ".prix_produit",
        ".priceProduct",
        "div.price"
    ),
    availabilitySelectors = listOf(
        "#spanStock",
        ".stock",
        ".availability"
    )
)
