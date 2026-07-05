package com.pctracker.ui.links

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductLinksScreen(componentId: Long, onBack: () -> Unit) {
    val repo = rememberRepository()
    val viewModel: ProductLinksViewModel = viewModel(
        factory = viewModelFactory { initializer { ProductLinksViewModel(repo, componentId) } }
    )
    val links by viewModel.links.collectAsState()

    var selectedProvider by remember { mutableStateOf(Provider.LDLC) }
    var url by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liens produits") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Ajouter un lien", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedProvider.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fournisseur") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth().padding(top = 8.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    Provider.entries.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider.displayName) },
                            onClick = {
                                selectedProvider = provider
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = url,
                onValueChange = { newUrl ->
                    url = newUrl
                    Provider.fromUrl(newUrl)?.let { detected -> selectedProvider = detected }
                },
                label = { Text("URL de la fiche produit") },
                supportingText = { Text("Le fournisseur est detecte automatiquement depuis l'URL") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label (optionnel)") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Button(
                onClick = {
                    if (url.isNotBlank()) {
                        viewModel.addLink(selectedProvider, url.trim(), label.trim())
                        url = ""
                        label = ""
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Ajouter")
            }

            Text("Liens existants", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(links, key = { it.id }) { link ->
                    val provider = runCatching { Provider.valueOf(link.provider) }.getOrNull()
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(provider?.displayName ?: link.provider, style = MaterialTheme.typography.bodyLarge)
                                Text(link.url, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                            Switch(checked = link.enabled, onCheckedChange = { viewModel.toggleEnabled(link) })
                            IconButton(onClick = { viewModel.delete(link) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                            }
                        }
                    }
                }
            }
        }
    }
}
