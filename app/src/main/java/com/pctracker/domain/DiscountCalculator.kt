package com.pctracker.domain

import com.pctracker.data.Provider
import com.pctracker.data.db.entity.ComponentEntity

/**
 * Picks the cheapest provider per component after applying each provider's discount, then
 * decides which components to route through Fnac/Darty/Boulanger in order to fully consume
 * the fixed-amount gift voucher at the lowest extra cost.
 *
 * The voucher is a fixed euro credit only usable at Fnac/Darty/Boulanger, so it is only
 * worth anything for components actually bought there. Rather than always taking the
 * cheapest provider component-by-component, we look for the components where switching to
 * Fnac/Darty costs the least extra (the "delta"), and reroute those first - this mirrors the
 * user's own priority ("petits ecarts : SSD, RAM, peripheriques plutot que CPU/GPU/carte
 * mere") without hardcoding categories, since low-cost/low-delta parts are naturally picked
 * first by the ascending sort.
 */
object DiscountCalculator {

    fun compute(
        components: List<ComponentEntity>,
        offersByComponent: Map<Long, List<ComponentOffer>>,
        voucherBalance: Double
    ): BestConfigResult {
        val missing = mutableListOf<ComponentEntity>()

        data class Choice(var provider: Provider?, var netPrice: Double?, var url: String?, var routedForVoucher: Boolean = false)

        val choices = mutableMapOf<Long, Choice>()
        val offersFor = mutableMapOf<Long, List<ComponentOffer>>()

        for (component in components) {
            val offers = offersByComponent[component.id].orEmpty()
            offersFor[component.id] = offers
            val cheapest = offers.minByOrNull { it.netPrice }
            if (cheapest == null) {
                missing += component
                choices[component.id] = Choice(null, null, null)
            } else {
                choices[component.id] = Choice(cheapest.provider, cheapest.netPrice, cheapest.productUrl)
            }
        }

        var fnacCartSubtotal = components
            .mapNotNull { choices[it.id] }
            .filter { it.provider == Provider.FNAC_DARTY }
            .sumOf { it.netPrice ?: 0.0 }

        data class Candidate(val componentId: Long, val delta: Double, val fnacOffer: ComponentOffer)

        val candidates = components.mapNotNull { component ->
            val choice = choices[component.id] ?: return@mapNotNull null
            if (choice.provider == Provider.FNAC_DARTY || choice.netPrice == null) return@mapNotNull null
            val fnacOffer = offersFor[component.id].orEmpty().firstOrNull { it.provider == Provider.FNAC_DARTY }
                ?: return@mapNotNull null
            val delta = fnacOffer.netPrice - choice.netPrice!!
            Candidate(component.id, delta, fnacOffer)
        }.sortedBy { it.delta }

        if (voucherBalance > 0.0) {
            for (candidate in candidates) {
                if (fnacCartSubtotal >= voucherBalance) break
                val choice = choices.getValue(candidate.componentId)
                choice.provider = Provider.FNAC_DARTY
                choice.netPrice = candidate.fnacOffer.netPrice
                choice.url = candidate.fnacOffer.productUrl
                choice.routedForVoucher = true
                fnacCartSubtotal += candidate.fnacOffer.netPrice
            }
        }

        val subtotal = components.sumOf { choices[it.id]?.netPrice ?: 0.0 }
        val voucherApplied = minOf(voucherBalance, fnacCartSubtotal).coerceAtLeast(0.0)
        val total = subtotal - voucherApplied

        val perComponent = components.map { component ->
            val choice = choices.getValue(component.id)
            ComponentBestPrice(
                component = component,
                offers = offersFor[component.id].orEmpty(),
                chosenProvider = choice.provider,
                chosenNetPrice = choice.netPrice,
                chosenUrl = choice.url,
                routedForVoucher = choice.routedForVoucher
            )
        }

        return BestConfigResult(
            perComponent = perComponent,
            subtotalBeforeVoucher = subtotal,
            voucherApplied = voucherApplied,
            total = total,
            missingComponents = missing
        )
    }
}
