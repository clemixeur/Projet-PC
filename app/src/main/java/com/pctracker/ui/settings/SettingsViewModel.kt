package com.pctracker.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pctracker.data.Provider
import com.pctracker.data.db.entity.AppSettingsEntity
import com.pctracker.data.db.entity.ProviderSettingEntity
import com.pctracker.data.db.entity.VoucherEntity
import com.pctracker.data.repository.PriceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun exportBackup(uri: Uri, context: Context, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val success = withContext(Dispatchers.IO) {
            runCatching {
                val json = repo.exportBackup()
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                    ?: error("Impossible d'ouvrir le fichier")
            }.isSuccess
        }
        onResult(success)
    }

    fun importBackup(uri: Uri, context: Context, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val success = withContext(Dispatchers.IO) {
            runCatching {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("Impossible de lire le fichier")
                repo.importBackup(json)
            }.isSuccess
        }
        onResult(success)
    }
}
