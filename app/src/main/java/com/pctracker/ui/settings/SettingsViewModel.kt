package com.pctracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pctracker.data.Provider
import com.pctracker.data.db.entity.AppSettingsEntity
import com.pctracker.data.db.entity.ProviderSettingEntity
import com.pctracker.data.db.entity.VoucherEntity
import com.pctracker.data.repository.PriceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: PriceRepository) : ViewModel() {

    val providerSettings: StateFlow<List<ProviderSettingEntity>> = repo.providerSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voucher: StateFlow<VoucherEntity?> = repo.voucher
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appSettings: StateFlow<AppSettingsEntity?> = repo.appSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateProviderDiscount(provider: Provider, percent: Double) = viewModelScope.launch {
        repo.updateProviderDiscount(provider, percent)
    }

    fun updateVoucher(balance: Double, expirationDate: Long?) = viewModelScope.launch {
        repo.updateVoucher(balance, expirationDate)
    }

    fun updateThreshold(value: Double) = viewModelScope.launch { repo.updateThreshold(value) }

    fun setAutoThreshold(enabled: Boolean) = viewModelScope.launch { repo.setAutoThresholdEnabled(enabled) }

    fun setScrapeIntervalHours(hours: Int) = viewModelScope.launch { repo.setScrapeIntervalHours(hours) }
}
