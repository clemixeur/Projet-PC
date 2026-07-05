package com.pctracker.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pctracker.data.Provider
import com.pctracker.ui.rememberRepository
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val repo = rememberRepository()
    val viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory { initializer { SettingsViewModel(repo) } }
    )

    val providerSettings by viewModel.providerSettings.collectAsState()
    val voucher by viewModel.voucher.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reglages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            item {
                Text("Remises par fournisseur", style = MaterialTheme.typography.titleMedium)
            }
            items(providerSettings, key = { it.provider }) { setting ->
                val provider = runCatching { Provider.valueOf(setting.provider) }.getOrNull()
                var text by remember(setting.provider, setting.discountPercent) {
                    mutableStateOf(setting.discountPercent.toString())
                }
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(provider?.displayName ?: setting.provider, style = MaterialTheme.typography.bodyLarge)
                            Text(setting.note, style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier.width(80.dp),
                            suffix = { Text("%") }
                        )
                        IconButton(onClick = {
                            provider?.let { p ->
                                text.toDoubleOrNull()?.let { viewModel.updateProviderDiscount(p, it) }
                            }
                        }) {
                            Icon(Icons.Filled.Check, contentDescription = "Enregistrer")
                        }
                    }
                }
            }

            item {
                Text("Bon d'achat Fnac/Darty/Boulanger", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            }
            item {
                VoucherEditor(
                    balance = voucher?.balance ?: 0.0,
                    initialAmount = voucher?.initialAmount ?: 130.0,
                    expirationDate = voucher?.expirationDate,
                    onSave = { balance, expiration -> viewModel.updateVoucher(balance, expiration) }
                )
            }

            item {
                Text("Seuil de bonne affaire", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            }
            item {
                ThresholdEditor(
                    threshold = appSettings?.thresholdTotal,
                    autoEnabled = appSettings?.autoThresholdEnabled ?: true,
                    onThresholdChange = { viewModel.updateThreshold(it) },
                    onAutoToggle = { viewModel.setAutoThreshold(it) }
                )
            }

            item {
                Text("Frequence de verification", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            }
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                IntervalEditor(
                    hours = appSettings?.scrapeIntervalHours ?: 3,
                    onChange = {
                        viewModel.setScrapeIntervalHours(it)
                        com.pctracker.work.WorkScheduler.schedule(context, it)
                    }
                )
            }

            item {
                Text("Sauvegarde", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            }
            item {
                BackupEditor(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun BackupEditor(viewModel: SettingsViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            viewModel.exportBackup(uri, context) { success ->
                val message = if (success) "Sauvegarde exportee" else "Echec de l'export"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.importBackup(uri, context) { success ->
                val message = if (success) "Sauvegarde importee" else "Echec de l'import (fichier invalide ?)"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Exporte les liens produits, remises, bon d'achat et seuil dans un fichier, " +
                    "pour les restaurer apres une reinstallation ou sur un autre appareil.",
                style = MaterialTheme.typography.bodySmall
            )
            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { exportLauncher.launch("pc-price-tracker-backup.json") }) {
                    Text("Exporter")
                }
                Button(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Importer")
                }
            }
        }
    }
}

@Composable
private fun VoucherEditor(
    balance: Double,
    initialAmount: Double,
    expirationDate: Long?,
    onSave: (Double, Long?) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }
    var balanceText by remember(balance) { mutableStateOf(balance.toString()) }
    var dateText by remember(expirationDate) {
        mutableStateOf(expirationDate?.let { dateFormat.format(it) } ?: "")
    }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Montant initial: ${initialAmount} €", style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Solde restant (€)") },
                    modifier = Modifier.weight(1f).padding(top = 8.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Expiration (jj/mm/aaaa)") },
                    modifier = Modifier.weight(1f).padding(top = 8.dp)
                )
                IconButton(onClick = {
                    val parsedBalance = balanceText.toDoubleOrNull() ?: balance
                    val parsedDate = runCatching { dateFormat.parse(dateText)?.time }.getOrNull()
                    onSave(parsedBalance, parsedDate)
                }) {
                    Icon(Icons.Filled.Check, contentDescription = "Enregistrer")
                }
            }
            expirationDate?.let {
                val daysLeft = TimeUnit.MILLISECONDS.toDays(it - System.currentTimeMillis())
                if (daysLeft in 0..30) {
                    Text(
                        "Attention: expire dans $daysLeft jour(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (daysLeft < 0) {
                    Text("Bon d'achat expire", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ThresholdEditor(
    threshold: Double?,
    autoEnabled: Boolean,
    onThresholdChange: (Double) -> Unit,
    onAutoToggle: (Boolean) -> Unit
) {
    var text by remember(threshold) { mutableStateOf(threshold?.toString() ?: "") }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Calcul automatique (moyenne 7 jours - 9%)", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = autoEnabled, onCheckedChange = onAutoToggle)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Seuil manuel (€)") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { text.toDoubleOrNull()?.let(onThresholdChange) }) {
                    Icon(Icons.Filled.Check, contentDescription = "Enregistrer")
                }
            }
        }
    }
}

@Composable
private fun IntervalEditor(hours: Int, onChange: (Int) -> Unit) {
    var text by remember(hours) { mutableStateOf(hours.toString()) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Toutes les X heures (1-24)") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { text.toIntOrNull()?.let(onChange) }) {
                Icon(Icons.Filled.Check, contentDescription = "Enregistrer")
            }
        }
    }
}
