package com.pctracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pctracker.ui.rememberRepository
import com.pctracker.work.WorkScheduler
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenLinks: (Long) -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    val repo = rememberRepository()
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory { initializer { HomeViewModel(repo) } }
    )
    val context = LocalContext.current

    val bestConfig by viewModel.bestConfig.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PC Price Tracker") },
                actions = {
                    IconButton(onClick = { WorkScheduler.triggerOneOff(context) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualiser maintenant")
                    }
                    IconButton(onClick = onOpenDiagnostics) {
                        Icon(Icons.Filled.BugReport, contentDescription = "Diagnostic scraping")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Reglages")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val config = bestConfig
            val threshold = settings?.thresholdTotal

            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total configuration la moins chere", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = config?.let { String.format(Locale.FRANCE, "%.2f €", it.total) } ?: "-",
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (config != null && threshold != null && config.total < threshold)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = threshold?.let { "Seuil: ${String.format(Locale.FRANCE, "%.2f €", it)}" } ?: "Seuil non defini",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (config != null && config.voucherApplied > 0.0) {
                        Text(
                            "dont bon d'achat applique: ${String.format(Locale.FRANCE, "%.2f €", config.voucherApplied)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (config != null && config.missingComponents.isNotEmpty()) {
                        Text(
                            "${config.missingComponents.size} composant(s) sans prix - ajoutez des liens produits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(config?.perComponent.orEmpty(), key = { it.component.id }) { item ->
                    val history by viewModel.historyFor(item.component.id).collectAsState()
                    ComponentCard(
                        item = item,
                        history = history,
                        onManageLinks = { onOpenLinks(item.component.id) }
                    )
                }
            }
        }
    }
}
