package com.pctracker.ui.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pctracker.ui.rememberRepository
import com.pctracker.ui.theme.StatusCriticalLight
import com.pctracker.ui.theme.StatusGoodLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(onBack: () -> Unit) {
    val repo = rememberRepository()
    val viewModel: DiagnosticsViewModel = viewModel(
        factory = viewModelFactory { initializer { DiagnosticsViewModel(repo) } }
    )
    val rows by viewModel.rows.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm:ss", Locale.FRANCE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnostic scraping") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        if (rows.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "Aucune tentative de scraping enregistree pour le moment.\n" +
                        "Verifie qu'au moins un lien produit est renseigne, puis appuie sur Actualiser sur l'ecran principal.",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                items(rows) { row ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (row.success) StatusGoodLight else StatusCriticalLight)
                                    )
                                    Text(
                                        "  ${row.providerLabel} - ${row.componentLabel}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Text(dateFormat.format(Date(row.timestamp)), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                row.message ?: "-",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (row.success) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                            )
                            row.productUrl?.let { url ->
                                Text(
                                    url,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
