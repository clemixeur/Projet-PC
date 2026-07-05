package com.pctracker.ui.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pctracker.data.Provider
import com.pctracker.data.db.entity.ScrapeLogEntity
import com.pctracker.data.repository.PriceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DiagnosticsRow(
    val timestamp: Long,
    val providerLabel: String,
    val componentLabel: String,
    val productUrl: String?,
    val success: Boolean,
    val message: String?
)

class DiagnosticsViewModel(repo: PriceRepository) : ViewModel() {

    val rows: StateFlow<List<DiagnosticsRow>> = combine(
        repo.recentScrapeLogs(200),
        repo.components
    ) { logs: List<ScrapeLogEntity>, components ->
        val nameById = components.associateBy({ it.id }, { it.displayName })
        logs.map { log ->
            DiagnosticsRow(
                timestamp = log.timestamp,
                providerLabel = runCatching { Provider.valueOf(log.provider) }.getOrNull()?.displayName ?: log.provider,
                componentLabel = log.componentId?.let { nameById[it] } ?: "?",
                productUrl = log.productUrl,
                success = log.success,
                message = log.message
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
