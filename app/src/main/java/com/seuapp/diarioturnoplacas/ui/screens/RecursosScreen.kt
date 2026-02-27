package com.seuapp.diarioturnoplacas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
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
import com.seuapp.diarioturnoplacas.ui.components.AppSecondaryButton
import com.seuapp.diarioturnoplacas.ui.components.AppSectionHeader
import com.seuapp.diarioturnoplacas.ui.components.AppTextField
import com.seuapp.diarioturnoplacas.ui.components.EmptyState

@Composable
fun RecursosScreen(state: AppUiState, vm: MainViewModel) {
    var supMenu by remember { mutableStateOf(false) }
    var dataTxt by remember(state.selectedDate) { mutableStateOf(state.selectedDate) }
    var tarefaMenu by remember { mutableStateOf(false) }
    var equipMenu by remember { mutableStateOf(false) }
    var moMenu by remember { mutableStateOf(false) }
    var tarefaSelecionada by remember { mutableStateOf<Int?>(null) }
    var equipamentoSelecionado by remember { mutableStateOf<Int?>(null) }
    var moSelecionado by remember { mutableStateOf<Int?>(null) }

    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            AppCard(title = "Recursos do Turno", icon = Icons.Filled.People) {
                AppSectionHeader(
                    title = "Supervisor, turno e data",
                    subtitle = "Defina o contexto e salve rascunho",
                    icon = Icons.Filled.Badge
                )

                AppSecondaryButton(
                    text = "Supervisor: ${state.supervisors.find { it.id == state.selectedSupervisorId }?.nome ?: "Selecionar"}",
                    icon = Icons.Filled.Badge
                ) { supMenu = true }

                DropdownMenu(expanded = supMenu, onDismissRequest = { supMenu = false }) {
                    state.supervisors.forEach { sup ->
                        DropdownMenuItem(
                            text = { Text(sup.nome) },
                            onClick = {
                                vm.setContext(sup.id, dataTxt, state.selectedJanela)
                                supMenu = false
                            }
                        )
                    }
                }

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("07x19", "19x07").forEachIndexed { index, janela ->
                        SegmentedButton(
                            shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(index, 2),
                            selected = state.selectedJanela == janela,
                            onClick = { vm.setContext(state.selectedSupervisorId, dataTxt, janela) },
                            label = { Text(janela) }
                        )
                    }
                }

                AppTextField(
                    value = dataTxt,
                    label = "Data (AAAA-MM-DD)",
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { dataTxt = it }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppSecondaryButton(text = "Aplicar") {
                        vm.setContext(state.selectedSupervisorId, dataTxt, state.selectedJanela)
                    }
                    AppPrimaryButton(text = "Salvar", icon = Icons.Filled.Save) {
                        vm.salvarRascunhoContexto()
                    }
                }
            }
        }

        item {
            AppSectionHeader(
                title = "Mao de obra",
                subtitle = "Cards por funcao com contador e adicao",
                icon = Icons.Filled.People
            )
        }

        if (state.funcoes.isEmpty()) {
            item {
                EmptyState(Icons.Filled.People, "Sem funcoes cadastradas", "Cadastre funcoes para alocar colaboradores")
            }
        }

        items(state.funcoes, key = { "func_${it.id}" }) { funcao ->
            val alocacao = state.alocacoes.find { it.funcaoId == funcao.id }
            val selecionados = alocacao?.selecionadosCsv?.split(",")?.mapNotNull { s -> s.toIntOrNull() } ?: emptyList()
            val total = selecionados.size + if (alocacao?.funcionarioOutraLetraId != null && !selecionados.contains(alocacao.funcionarioOutraLetraId)) 1 else 0
            var menuOutra by remember(funcao.id) { mutableStateOf(false) }

            AppCard(title = funcao.nomeFuncao, icon = Icons.Filled.People) {
                Text("Alocados: $total / ${funcao.efetivoPrevisto}", fontWeight = FontWeight.SemiBold)
                state.funcionarios.filter { it.funcaoId == funcao.id }.forEach { f ->
                    Row {
                        Checkbox(
                            checked = selecionados.contains(f.id),
                            onCheckedChange = { checked -> vm.toggleFuncionario(funcao.id, f.id, checked) }
                        )
                        Text("${f.nome} (${f.letra})", modifier = Modifier.padding(top = 12.dp))
                    }
                }
                AppSecondaryButton(text = "Funcionario de outra letra", icon = Icons.Filled.Badge) {
                    menuOutra = true
                }
                DropdownMenu(expanded = menuOutra, onDismissRequest = { menuOutra = false }) {
                    DropdownMenuItem(text = { Text("Nenhum") }, onClick = {
                        vm.setFuncionarioOutraLetra(funcao.id, null)
                        menuOutra = false
                    })
                    state.funcionarios.filter { it.funcaoId == funcao.id }.forEach { cand ->
                        DropdownMenuItem(text = { Text("${cand.nome} (${cand.letra})") }, onClick = {
                            vm.setFuncionarioOutraLetra(funcao.id, cand.id)
                            menuOutra = false
                        })
                    }
                }
            }
        }

        item {
            AppSectionHeader(
                title = "Equipamentos",
                subtitle = "ADM indisponivel em fim de semana/feriado/19x07",
                icon = Icons.Filled.Construction
            )
        }

        item {
            if (state.recursos.isEmpty()) {
                EmptyState(Icons.Filled.Construction, "Sem equipamentos", "Cadastre equipamentos para selecao")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.recursos.forEach { rec ->
                        val selected = state.recursosSelecionados.any { it.id == rec.id }
                        val indisponivel = vm.recursoIndisponivelPorRegra(rec)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (!indisponivel) {
                                    vm.toggleRecurso(rec.id, !selected)
                                }
                            },
                            enabled = !indisponivel,
                            label = {
                                val admLabel = if (indisponivel) " | ADM - indisponivel neste periodo" else ""
                                Text("${rec.tag} (${rec.tipo})$admLabel")
                            }
                        )
                    }
                }
            }
        }

        item {
            AppSectionHeader(
                title = "Alocacao por atividade",
                subtitle = "Vincular tarefa + equipamento opcional + mao de obra opcional",
                icon = Icons.Filled.Build
            )
        }

        item {
            AppCard(title = "Novo vinculo", icon = Icons.Filled.Build) {
                AppSecondaryButton(
                    text = "Tarefa: ${state.tarefas.find { it.id == tarefaSelecionada }?.descricao ?: "Selecionar"}",
                    icon = Icons.Filled.Build
                ) { tarefaMenu = true }
                DropdownMenu(expanded = tarefaMenu, onDismissRequest = { tarefaMenu = false }) {
                    state.tarefas.forEach { t ->
                        DropdownMenuItem(text = { Text(t.descricao) }, onClick = {
                            tarefaSelecionada = t.id
                            tarefaMenu = false
                        })
                    }
                }

                AppSecondaryButton(
                    text = "Equipamento: ${state.recursos.find { it.id == equipamentoSelecionado }?.tag ?: "Opcional"}",
                    icon = Icons.Filled.Construction
                ) { equipMenu = true }
                DropdownMenu(expanded = equipMenu, onDismissRequest = { equipMenu = false }) {
                    DropdownMenuItem(text = { Text("Nenhum") }, onClick = { equipamentoSelecionado = null; equipMenu = false })
                    state.recursosSelecionados.forEach { e ->
                        DropdownMenuItem(text = { Text("${e.tag} (${e.tipo})") }, onClick = {
                            equipamentoSelecionado = e.id
                            equipMenu = false
                        })
                    }
                }

                AppSecondaryButton(
                    text = "Mao de obra: ${state.funcionarios.find { it.id == moSelecionado }?.nome ?: "Opcional"}",
                    icon = Icons.Filled.People
                ) { moMenu = true }
                DropdownMenu(expanded = moMenu, onDismissRequest = { moMenu = false }) {
                    DropdownMenuItem(text = { Text("Nenhum") }, onClick = { moSelecionado = null; moMenu = false })
                    state.funcionarios.forEach { f ->
                        DropdownMenuItem(text = { Text(f.nome) }, onClick = {
                            moSelecionado = f.id
                            moMenu = false
                        })
                    }
                }

                AppPrimaryButton(text = "Vincular", icon = Icons.Filled.Save, enabled = tarefaSelecionada != null) {
                    tarefaSelecionada?.let { tarefa ->
                        vm.addAlocacaoAtividade(tarefa, equipamentoSelecionado, moSelecionado)
                    }
                }
            }
        }

        itemsIndexed(state.alocacoesAtividade, key = { _, item -> "alv_${item.id}" }) { _, item ->
            AppCard {
                val tarefa = state.tarefas.find { it.id == item.tarefaId }?.descricao ?: "Tarefa #${item.tarefaId}"
                val equip = item.equipamentoId?.let { id -> state.recursos.find { it.id == id }?.tag } ?: "-"
                val mo = item.maoObraId?.let { id -> state.funcionarios.find { it.id == id }?.nome } ?: "-"
                Text(tarefa, fontWeight = FontWeight.SemiBold)
                Text("Equipamento: $equip")
                Text("MO: $mo")
                AppSecondaryButton(text = "Remover") { vm.removeAlocacaoAtividade(item.id) }
            }
        }
    }
}
