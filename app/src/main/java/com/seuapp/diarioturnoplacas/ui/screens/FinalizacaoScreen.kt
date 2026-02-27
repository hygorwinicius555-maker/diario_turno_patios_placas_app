package com.seuapp.diarioturnoplacas.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.seuapp.diarioturnoplacas.ui.components.AppTextField
import com.seuapp.diarioturnoplacas.ui.components.CapacityGauge
import com.seuapp.diarioturnoplacas.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalizacaoScreen(state: AppUiState, vm: MainViewModel) {
    var showHistory by remember { mutableStateOf(false) }
    var expandedCardId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AppSectionHeader(
                    title = "Finalizacao do turno",
                    subtitle = "Preencher atendido, observacoes e concluir turno",
                    icon = Icons.Filled.Done
                )
                IconButton(onClick = { showHistory = true }) {
                    Icon(Icons.Filled.History, contentDescription = "Historico")
                }
            }
        }

        if (state.tarefas.isEmpty()) {
            item {
                EmptyState(Icons.Filled.Timer, "Sem programacao", "Cadastre tarefas na aba Programacao antes de finalizar")
            }
        }

        items(state.tarefas, key = { "fim_${it.id}" }) { tarefa ->
            var qtdTxt by remember(tarefa.id, tarefa.quantidadeExecutada) { mutableStateOf(tarefa.quantidadeExecutada.toString()) }
            var obsTxt by remember(tarefa.id, tarefa.observacaoTurno) { mutableStateOf(tarefa.observacaoTurno) }

            AppCard(title = tarefa.descricao, icon = Icons.Filled.Timer) {
                Text("Programado: ${tarefa.quantidade}", fontWeight = FontWeight.SemiBold)
                AppTextField(
                    value = qtdTxt,
                    label = "Atendido",
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = {
                        qtdTxt = it.filter(Char::isDigit)
                        vm.updateTarefa(tarefa, quantidadeExecutada = qtdTxt.toIntOrNull() ?: 0)
                    }
                )
                AppTextField(
                    value = obsTxt,
                    label = "Observacao",
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = {
                        obsTxt = it
                        vm.updateTarefa(tarefa, observacao = it)
                    }
                )
            }
        }

        item {
            AppPrimaryButton(text = "FINALIZAR TURNO", icon = Icons.Filled.Done, enabled = state.canFinalize) {
                vm.finalizarTurno()
            }
        }
    }

    if (showHistory) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { showHistory = false }, sheetState = sheetState) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppSectionHeader("Consulta de historico", icon = Icons.Filled.History)
                state.historicoTurnos.forEach { row ->
                    val programado = row.programado ?: 0
                    val executado = row.executado ?: 0
                    val cumprimento = if (programado > 0) executado.toDouble() / programado.toDouble() else 0.0
                    val expandido = expandedCardId == row.turnoId

                    AppCard(modifier = Modifier.fillMaxWidth(), title = "${row.data} - ${row.janela}") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(row.supervisorNome ?: "Sem supervisor")
                            Text(
                                if ((row.acimaCapacidadePct ?: 0.0) > 0) "${"%.1f".format(row.acimaCapacidadePct)}% acima" else "Dentro capacidade",
                                color = if ((row.acimaCapacidadePct ?: 0.0) > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                        CapacityGauge(value = cumprimento, label = "${"%.0f".format(cumprimento * 100)}%")
                        Text("Programado: $programado | Atendido: $executado")
                        Text(
                            if (expandido) "Ocultar detalhes" else "Ver detalhes",
                            modifier = Modifier.clickable { expandedCardId = if (expandido) null else row.turnoId },
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (expandido) {
                            Text("Ocorrencias: ${row.ocorrencias}")
                            row.ultimoEvento?.let { Text("Ultima ocorrencia: $it") }
                            Text("Toque para abrir este turno")
                            Row {
                                AppPrimaryButton(text = "Abrir turno") {
                                    vm.selecionarTurno(row.turnoId)
                                    showHistory = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
