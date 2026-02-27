package com.seuapp.diarioturnoplacas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seuapp.diarioturnoplacas.ui.components.AppCard
import com.seuapp.diarioturnoplacas.ui.components.AppSectionHeader
import com.seuapp.diarioturnoplacas.ui.components.CapacityGauge
import com.seuapp.diarioturnoplacas.ui.components.EmptyState

@Composable
fun CapacidadeScreen(state: AppUiState) {
    val m = state.metrics
    val usoPercent = (m.percentualUso * 100.0)
    val demanda = m.demandaMin
    val capacidade = m.capacidadeMin
    val saldo = capacidade - demanda

    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            AppSectionHeader(
                title = "Capacidade x Demanda",
                subtitle = "Efetivo por atividade com base na alocacao",
                icon = Icons.Filled.Analytics
            )
        }

        if (m.acimaCapacidade) {
            item {
                AppCard(title = "DEMANDAS ACIMA DA CAPACIDADE", icon = Icons.Filled.Warning) {
                    Text(
                        "${"%.1f".format(m.percentualAcima * 100)}% acima da capacidade",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Text(m.causa)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AppCard(modifier = Modifier.weight(1f), title = "Capacidade", icon = Icons.Filled.Speed) {
                    Text("${"%.1f".format(capacidade)} min/h")
                }
                AppCard(modifier = Modifier.weight(1f), title = "Demanda", icon = Icons.Filled.Analytics) {
                    Text("${"%.1f".format(demanda)} min/h")
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AppCard(modifier = Modifier.weight(1f), title = "Saldo") {
                    Text("${"%.1f".format(saldo)} min/h")
                }
                AppCard(modifier = Modifier.weight(1f), title = "% acima") {
                    Text(if (m.percentualAcima > 0) "${"%.1f".format(m.percentualAcima * 100)}%" else "0%")
                }
            }
        }

        item {
            AppCard(title = "Gauge de uso", icon = Icons.Filled.Analytics) {
                CapacityGauge(value = m.percentualUso, label = "${"%.0f".format(usoPercent)}%")
                LinearProgressIndicator(
                    progress = { m.percentualUso.coerceIn(0.0, 1.0).toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (m.porAtividade.isEmpty()) {
            item {
                EmptyState(Icons.Filled.Analytics, "Sem atividades alocadas", "Vincule atividades na aba Recursos")
            }
        }

        items(m.porAtividade, key = { "atv_${it.tarefaId}" }) { atv ->
            AppCard(title = atv.descricao, icon = Icons.Filled.Analytics) {
                Text("Alocacoes: ${atv.alocacoes}")
                Text("Demanda: ${"%.1f".format(atv.demandaMin)} min")
                Text("Capacidade: ${"%.1f".format(atv.capacidadeMin)} min")
                Text(
                    if (atv.acimaCapacidade) "Acima da capacidade" else "Dentro da capacidade",
                    color = if (atv.acimaCapacidade) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
