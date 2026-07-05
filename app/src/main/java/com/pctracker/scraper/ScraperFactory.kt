package com.pctracker.scraper

import com.pctracker.data.Provider

object ScraperFactory {
    private val scrapers: Map<Provider, ProviderScraper> = mapOf(
        Provider.LDLC to LdlcScraper(),
        Provider.MATERIEL_NET to MaterielNetScraper(),
        Provider.TOPACHAT to TopachatScraper(),
        Provider.FNAC_DARTY to FnacDartyScraper(),
        Provider.AMAZON to AmazonScraper()
    )

    fun forProvider(provider: Provider): ProviderScraper = scrapers.getValue(provider)
}
