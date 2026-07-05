package com.pctracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pctracker.data.db.entity.AppSettingsEntity
import com.pctracker.data.db.entity.PriceSnapshotEntity
import com.pctracker.data.db.entity.TotalHistoryEntity
import com.pctracker.data.db.entity.VoucherEntity
import com.pctracker.data.repository.PriceRepository
import com.pctracker.domain.BestConfigResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: PriceRepository) : ViewModel() {

    val bestConfig: StateFlow<BestConfigResult?> = repo.bestConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appSettings: StateFlow<AppSettingsEntity?> = repo.appSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val voucher: StateFlow<VoucherEntity?> = repo.voucher
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalHistory: StateFlow<List<TotalHistoryEntity>> = repo.totalHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val historyCache = mutableMapOf<Long, StateFlow<List<Double>>>()

    fun historyFor(componentId: Long): StateFlow<List<Double>> = historyCache.getOrPut(componentId) {
        repo.historyForComponent(componentId)
            .map { snapshots -> toMinPriceTrend(snapshots) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    private fun toMinPriceTrend(snapshots: List<PriceSnapshotEntity>): List<Double> =
        snapshots
            .filter { it.available && it.rawPrice != null }
            .groupBy { it.timestamp }
            .toSortedMap()
            .map { (_, group) -> group.minOf { it.rawPrice!! } }

    fun updateThreshold(value: Double) = viewModelScope.launch { repo.updateThreshold(value) }

    fun setAutoThreshold(enabled: Boolean) = viewModelScope.launch { repo.setAutoThresholdEnabled(enabled) }
}
