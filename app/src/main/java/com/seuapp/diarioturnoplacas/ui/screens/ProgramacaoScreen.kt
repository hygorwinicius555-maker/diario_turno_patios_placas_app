package com.seuapp.diarioturnoplacas.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seuapp.diarioturnoplacas.ui.components.AppCard
import com.seuapp.diarioturnoplacas.ui.components.AppPrimaryButton
import com.seuapp.diarioturnoplacas.ui.components.AppSecondaryButton
import com.seuapp.diarioturnoplacas.ui.components.AppSectionHeader
import com.seuapp.diarioturnoplacas.ui.components.AppTextField
import com.seuapp.diarioturnoplacas.ui.components.EmptyState

@Composable
fun ProgramacaoScreen(state: AppUiState, vm: MainViewModel) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importExcel(uri)
    }
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("TAREFAS", "AJUSTES")

    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            AppCard(title = "Programacao", icon = Icons.Filled.Checklist) {
                AppSectionHeader(
                    title = "Fluxo de placas",
                    subtitle = "Selecione tarefas e ajuste quantidade/frequencia/regras",
                    icon = Icons.Filled.Factory
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppSecondaryButton(text = "Importar Excel", icon = Icons.Filled.Factory) {
                        picker.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    }
                    AppPrimaryButton(text = "Salvar", icon = Icons.Filled.Save) { vm.salvarRascunhoContexto() }
                }
            }
        }

        item {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, label ->
                    Tab(selected = tabIndex == index, onClick = { tabIndex = index }, text = { Text(label) })
                }
            }
        }

        if (tabIndex == 0) {
            item {
                AppSectionHeader("Selecionar tarefas", icon = Icons.Filled.Checklist)
            }
            items(vm.templatesProgramacao()) { template ->
                val selected = state.tarefas.any { it.descricao == template }
                AppCard {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(template, modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = selected,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    vm.addTarefaTemplate(template)
                                } else {
                                    state.tarefas.find { it.descricao == template }?.let { vm.removeTarefa(it.id) }
                                }
                            }
                        )
                    }
                }
            }
        } else {
            if (state.tarefas.isEmpty()) {
                item {
                    EmptyState(Icons.Filled.Tune, "Nenhuma tarefa selecionada", "Selecione tarefas na aba TAREFAS")
                }
            }
            items(state.tarefas, key = { "task_${it.id}" }) { tarefa ->
                var qtdTxt by remember(tarefa.id, tarefa.quantidade) { mutableStateOf(tarefa.quantidade.toString()) }
                var freqTxt by remember(tarefa.id, tarefa.frequencia) { mutableStateOf(tarefa.frequencia.toString()) }
                AppCard(title = tarefa.descricao, icon = Icons.Filled.Tune) {
                    Text("Categoria: ${tarefa.categoria}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppTextField(
                            value = qtdTxt,
                            label = "Quantidade",
                            modifier = Modifier.weight(1f),
                            onValueChange = {
                                qtdTxt = it.filter(Char::isDigit)
                                vm.updateTarefa(tarefa, quantidade = qtdTxt.toIntOrNull() ?: 1)
                            }
                        )
                        AppTextField(
                            value = freqTxt,
                            label = "Frequencia",
                            modifier = Modifier.weight(1f),
                            onValueChange = {
                                freqTxt = it.filter(Char::isDigit)
                                vm.updateTarefa(tarefa, frequencia = freqTxt.toIntOrNull() ?: 1)
                            }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppSecondaryButton(text = "Multiplicar") { vm.updateTarefa(tarefa, modoCalculo = "MULTIPLICAR") }
                        AppSecondaryButton(text = "Dividir") { vm.updateTarefa(tarefa, modoCalculo = "DIVIDIR") }
                        AppSecondaryButton(text = "Remover") { vm.removeTarefa(tarefa.id) }
                    }
                    Text("Tempo total: ${"%.1f".format(tarefa.tempoTotalMin)} min", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
