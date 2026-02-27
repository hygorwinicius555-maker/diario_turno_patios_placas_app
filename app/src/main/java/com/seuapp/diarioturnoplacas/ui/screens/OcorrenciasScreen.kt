package com.seuapp.diarioturnoplacas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seuapp.diarioturnoplacas.ui.components.AppCard
import com.seuapp.diarioturnoplacas.ui.components.AppPrimaryButton
import com.seuapp.diarioturnoplacas.ui.components.AppSectionHeader
import com.seuapp.diarioturnoplacas.ui.components.AppSecondaryButton
import com.seuapp.diarioturnoplacas.ui.components.AppTextField
import com.seuapp.diarioturnoplacas.ui.components.EmptyState

@Composable
fun OcorrenciasScreen(state: AppUiState, vm: MainViewModel) {
    val tipos = listOf(
        "Quebra de equipamento",
        "Efetivo passou mal",
        "Demanda extra",
        "Seguranca",
        "Geral"
    )

    var tipo by remember { mutableStateOf(tipos.last()) }
    var tipoMenu by remember { mutableStateOf(false) }
    var desc by remember { mutableStateOf("") }
    var equipamento by remember { mutableStateOf("") }
    var maoObra by remember { mutableStateOf("") }
    var filtroData by remember(state.selectedDate) { mutableStateOf(state.selectedDate) }

    val historicoFiltrado = state.ocorrenciasHistorico.filter { it.data == filtroData }

    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            AppSectionHeader(
                title = "Ocorrencias",
                subtitle = "Registro do turno atual e historico por data",
                icon = Icons.Filled.Report
            )
        }

        item {
            AppCard(title = "Nova ocorrencia", icon = Icons.Filled.Report) {
                AppSecondaryButton(text = "Tipo: $tipo", icon = Icons.Filled.Report) { tipoMenu = true }
                DropdownMenu(expanded = tipoMenu, onDismissRequest = { tipoMenu = false }) {
                    tipos.forEach { opt ->
                        DropdownMenuItem(text = { Text(opt) }, onClick = {
                            tipo = opt
                            tipoMenu = false
                        })
                    }
                }

                AppTextField(value = desc, label = "Descricao", modifier = Modifier.fillMaxWidth(), onValueChange = { desc = it })
                AppTextField(value = equipamento, label = "Equipamento relacionado (opcional)", modifier = Modifier.fillMaxWidth(), onValueChange = { equipamento = it })
                AppTextField(value = maoObra, label = "Mao de obra relacionada (opcional)", modifier = Modifier.fillMaxWidth(), onValueChange = { maoObra = it })

                AppPrimaryButton(text = "Salvar ocorrencia", icon = Icons.Filled.Build) {
                    vm.addOcorrencia(tipo, desc, equipamento.ifBlank { null }, maoObra.ifBlank { null })
                    desc = ""
                    equipamento = ""
                    maoObra = ""
                }
            }
        }

        item {
            AppSectionHeader("Turno atual", icon = Icons.Filled.MedicalServices)
        }

        if (state.ocorrenciasTurno.isEmpty()) {
            item {
                EmptyState(Icons.Filled.Report, "Sem ocorrencias no turno", "Quando houver eventos, eles aparecerao aqui")
            }
        }

        items(state.ocorrenciasTurno, key = { "occ_${it.id}" }) { oc ->
            AppCard {
                Text("${oc.horario} | ${oc.tipo}", fontWeight = FontWeight.SemiBold)
                Text(oc.descricao)
                if (!oc.equipamentoRelacionado.isNullOrBlank()) {
                    Text("Equipamento: ${oc.equipamentoRelacionado}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!oc.maoObraRelacionada.isNullOrBlank()) {
                    Text("MO: ${oc.maoObraRelacionada}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            AppSectionHeader("Historico", icon = Icons.Filled.Report)
            AppTextField(
                value = filtroData,
                label = "Filtrar por data (AAAA-MM-DD)",
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { filtroData = it }
            )
        }

        items(historicoFiltrado, key = { "hist_occ_${it.id}" }) { oc ->
            AppCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${oc.data} ${oc.horario}")
                    Text(oc.janela)
                }
                Text(oc.tipo, fontWeight = FontWeight.SemiBold)
                Text(oc.descricao)
            }
        }
    }
}
