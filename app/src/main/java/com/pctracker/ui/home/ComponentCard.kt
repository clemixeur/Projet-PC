package com.pctracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pctracker.domain.ComponentBestPrice
import java.util.Locale

@Composable
fun ComponentCard(
    item: ComponentBestPrice,
    history: List<Double>,
    onManageLinks: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.component.displayName, style = MaterialTheme.typography.titleMedium)
                    if (item.chosenProvider != null && item.chosenNetPrice != null) {
                        Text(
                            "${formatPrice(item.chosenNetPrice)} - ${item.chosenProvider.displayName}" +
                                if (item.routedForVoucher) " (bon d'achat)" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text("Aucun prix pour le moment", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                IconButton(onClick = onManageLinks) {
                    Icon(Icons.Filled.Link, contentDescription = "Gerer les liens produits")
                }
            }

            if (history.size >= 2) {
                MiniHistoryChart(values = history, modifier = Modifier.padding(top = 8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.chosenUrl != null) {
                    Button(onClick = { openUrl(context, item.chosenUrl) }) {
                        Text("Acheter")
                    }
                } else {
                    Text("")
                }
                if (item.offers.size > 1) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = "Voir tous les prix"
                        )
                    }
                }
            }

            if (expanded) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    item.offers.sortedBy { it.netPrice }.forEach { offer ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(offer.provider.displayName, style = MaterialTheme.typography.bodySmall)
                            Text(formatPrice(offer.netPrice), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private fun formatPrice(value: Double): String = String.format(Locale.FRANCE, "%.2f €", value)

private fun openUrl(context: android.content.Context, url: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
    context.startActivity(intent)
}
