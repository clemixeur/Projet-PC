package com.pctracker.ui.links

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pctracker.data.Provider
import com.pctracker.data.db.entity.ProductLinkEntity
import com.pctracker.data.repository.PriceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductLinksViewModel(
    private val repo: PriceRepository,
    private val componentId: Long
) : ViewModel() {

    val links: StateFlow<List<ProductLinkEntity>> = repo.productLinksForComponent(componentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addLink(provider: Provider, url: String, label: String?) = viewModelScope.launch {
        repo.addProductLink(componentId, provider, url, label?.ifBlank { null })
    }

    fun toggleEnabled(link: ProductLinkEntity) = viewModelScope.launch {
        repo.updateProductLink(link.copy(enabled = !link.enabled))
    }

    fun delete(link: ProductLinkEntity) = viewModelScope.launch {
        repo.deleteProductLink(link)
    }
}
