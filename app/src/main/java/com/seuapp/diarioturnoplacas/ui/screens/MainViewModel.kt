package com.seuapp.diarioturnoplacas.ui.screens

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.seuapp.diarioturnoplacas.data.db.AlocacaoAtividadeEntity
import com.seuapp.diarioturnoplacas.data.db.AlocacaoMOEntity
import com.seuapp.diarioturnoplacas.data.db.FuncaoMOEntity
import com.seuapp.diarioturnoplacas.data.db.FuncionarioEntity
import com.seuapp.diarioturnoplacas.data.db.OcorrenciaTurnoEntity
import com.seuapp.diarioturnoplacas.data.db.RecursoOperacionalEntity
import com.seuapp.diarioturnoplacas.data.db.SupervisorEntity
import com.seuapp.diarioturnoplacas.data.db.TarefaProgramadaEntity
import com.seuapp.diarioturnoplacas.data.db.TurnoEntity
import com.seuapp.diarioturnoplacas.data.db.TurnoHistoricoRow
import com.seuapp.diarioturnoplacas.data.db.TurnoResumoEntity
import com.seuapp.diarioturnoplacas.di.ServiceLocator
import com.seuapp.diarioturnoplacas.domain.model.CapacityMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

data class AppUiState(
    val selectedTurnoId: Int? = null,
    val currentTurno: TurnoEntity? = null,
    val selectedDate: String = LocalDate.now().toString(),
    val selectedJanela: String = "07x19",
    val selectedSupervisorId: Int? = null,
    val turnos: List<TurnoEntity> = emptyList(),
    val supervisors: List<SupervisorEntity> = emptyList(),
    val funcoes: List<FuncaoMOEntity> = emptyList(),
    val funcionarios: List<FuncionarioEntity> = emptyList(),
    val recursos: List<RecursoOperacionalEntity> = emptyList(),
    val recursosSelecionados: List<RecursoOperacionalEntity> = emptyList(),
    val tarefas: List<TarefaProgramadaEntity> = emptyList(),
    val alocacoes: List<AlocacaoMOEntity> = emptyList(),
    val alocacoesAtividade: List<AlocacaoAtividadeEntity> = emptyList(),
    val ocorrenciasTurno: List<OcorrenciaTurnoEntity> = emptyList(),
    val ocorrenciasHistorico: List<OcorrenciaTurnoEntity> = emptyList(),
    val minutesTurno: Int = 720,
    val metrics: CapacityMetrics = CapacityMetrics(0.0, 0.0, 0.0, 0.0, false, "DENTRO DA CAPACIDADE", emptyList()),
    val historicoTurnos: List<TurnoHistoricoRow> = emptyList(),
    val canFinalize: Boolean = false,
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel : ViewModel() {
    private val repo = ServiceLocator.appRepository
    private val settings = ServiceLocator.settingsRepository
    private val calculator = ServiceLocator.capacityCalculator

    private val selectedTurnoId = MutableStateFlow<Int?>(null)
    private val _uiState: MutableState<AppUiState> = mutableStateOf(AppUiState())
    val uiState: State<AppUiState> = _uiState

    private val templates = listOf(
        "Fluxo de placas - Recebimento",
        "Fluxo de placas - Movimentacao interna",
        "Fluxo de placas - Carregamento",
        "Fluxo de placas - Conferencia",
        "Fluxo de placas - Despacho"
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repo.seedIfNeeded()
        }

        viewModelScope.launch {
            repo.turnosFlow().collect { turnos ->
                if (selectedTurnoId.value == null && turnos.isNotEmpty()) {
                    selectedTurnoId.value = turnos.first().id
                }
            }
        }

        val tarefasFlow = selectedTurnoId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.tarefasTurnoFlow(id) }
        val alocacoesFlow = selectedTurnoId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.alocacoesTurnoFlow(id) }
        val alocAtivFlow = selectedTurnoId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.alocacoesAtividadeFlow(id) }
        val recursosSelecionadosFlow = selectedTurnoId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.recursosSelecionadosFlow(id) }
        val ocorrenciasFlow = selectedTurnoId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.ocorrenciasTurnoFlow(id) }

        viewModelScope.launch {
            combine(
                repo.turnosFlow(),
                repo.historicoTurnosFlow(),
                repo.supervisorsFlow(),
                repo.funcoesFlow(),
                repo.funcionariosFlow(),
                repo.recursosFlow(),
                repo.ocorrenciasFlow(),
                tarefasFlow,
                alocacoesFlow,
                alocAtivFlow,
                recursosSelecionadosFlow,
                ocorrenciasFlow,
                settings.settingsFlow,
                selectedTurnoId
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val turnos = values[0] as List<TurnoEntity>
                @Suppress("UNCHECKED_CAST")
                val historicoTurnos = values[1] as List<TurnoHistoricoRow>
                @Suppress("UNCHECKED_CAST")
                val supervisors = values[2] as List<SupervisorEntity>
                @Suppress("UNCHECKED_CAST")
                val funcoes = values[3] as List<FuncaoMOEntity>
                @Suppress("UNCHECKED_CAST")
                val funcionarios = values[4] as List<FuncionarioEntity>
                @Suppress("UNCHECKED_CAST")
                val recursos = values[5] as List<RecursoOperacionalEntity>
                @Suppress("UNCHECKED_CAST")
                val ocorrenciasTodas = values[6] as List<OcorrenciaTurnoEntity>
                @Suppress("UNCHECKED_CAST")
                val tarefas = values[7] as List<TarefaProgramadaEntity>
                @Suppress("UNCHECKED_CAST")
                val alocacoes = values[8] as List<AlocacaoMOEntity>
                @Suppress("UNCHECKED_CAST")
                val alocacoesAtividade = values[9] as List<AlocacaoAtividadeEntity>
                @Suppress("UNCHECKED_CAST")
                val recursosSelecionados = values[10] as List<RecursoOperacionalEntity>
                @Suppress("UNCHECKED_CAST")
                val ocorrenciasTurno = values[11] as List<OcorrenciaTurnoEntity>
                val appSettings = values[12] as com.seuapp.diarioturnoplacas.data.repository.AppSettings
                val turnoId = values[13] as Int?

                val currentTurno = turnos.find { it.id == turnoId }
                val metrics = calculator.calculate(tarefas, alocacoesAtividade, appSettings.minutosTurno)
                val selectedDate = currentTurno?.data ?: appSettings.ultimaData ?: LocalDate.now().toString()
                val selectedJanela = currentTurno?.janela ?: appSettings.ultimaJanela
                val selectedSupervisor = currentTurno?.supervisorId ?: appSettings.ultimoSupervisorId ?: supervisors.firstOrNull()?.id

                AppUiState(
                    selectedTurnoId = turnoId,
                    currentTurno = currentTurno,
                    selectedDate = selectedDate,
                    selectedJanela = selectedJanela,
                    selectedSupervisorId = selectedSupervisor,
                    turnos = turnos,
                    supervisors = supervisors,
                    funcoes = funcoes,
                    funcionarios = funcionarios,
                    recursos = recursos,
                    recursosSelecionados = recursosSelecionados,
                    tarefas = tarefas,
                    alocacoes = alocacoes,
                    alocacoesAtividade = alocacoesAtividade,
                    ocorrenciasTurno = ocorrenciasTurno,
                    ocorrenciasHistorico = ocorrenciasTodas,
                    minutesTurno = appSettings.minutosTurno,
                    metrics = metrics,
                    historicoTurnos = historicoTurnos,
                    canFinalize = selectedSupervisor != null && tarefas.isNotEmpty(),
                    message = _uiState.value.message
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun templatesProgramacao(): List<String> = templates

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun selecionarTurno(turnoId: Int) {
        selectedTurnoId.value = turnoId
    }

    fun setMinutosTurno(minutos: Int) {
        viewModelScope.launch { settings.setMinutosTurno(minutos.coerceAtLeast(60)) }
    }

    fun setContext(supervisorId: Int?, data: String, janela: String) {
        viewModelScope.launch {
            val validSupervisor = supervisorId ?: _uiState.value.supervisors.firstOrNull()?.id
            if (validSupervisor == null) {
                _uiState.value = _uiState.value.copy(message = "Cadastre um supervisor para continuar")
                return@launch
            }

            val existente = _uiState.value.turnos.firstOrNull {
                it.data == data && it.janela == janela && it.supervisorId == validSupervisor
            }

            val turnoId = existente?.id ?: repo.createTurno(data, janela, validSupervisor)
            settings.saveContext(validSupervisor, data, janela)
            selectedTurnoId.value = turnoId
        }
    }

    fun salvarRascunhoContexto() {
        val state = _uiState.value
        viewModelScope.launch {
            settings.saveContext(state.selectedSupervisorId, state.selectedDate, state.selectedJanela)
            _uiState.value = _uiState.value.copy(message = "Rascunho salvo")
        }
    }

    fun updateTarefa(
        tarefa: TarefaProgramadaEntity,
        quantidade: Int? = null,
        frequencia: Int? = null,
        modoCalculo: String? = null,
        quantidadeExecutada: Int? = null,
        observacao: String? = null
    ) {
        val qtd = quantidade ?: tarefa.quantidade
        val freq = (frequencia ?: tarefa.frequencia).coerceAtLeast(1)
        val modo = modoCalculo ?: tarefa.modoCalculo
        val tempoTotal = when (modo) {
            "DIVIDIR" -> (tarefa.tempoBaseMin / freq) * qtd
            else -> (tarefa.tempoBaseMin * freq) * qtd
        }

        viewModelScope.launch {
            repo.updateTarefa(
                tarefa.copy(
                    quantidade = qtd,
                    frequencia = freq,
                    modoCalculo = modo,
                    tempoTotalMin = tempoTotal,
                    quantidadeExecutada = quantidadeExecutada ?: tarefa.quantidadeExecutada,
                    observacaoTurno = observacao ?: tarefa.observacaoTurno,
                    statusExecucao = statusFromExecucao(quantidadeExecutada ?: tarefa.quantidadeExecutada, qtd)
                )
            )
        }
    }

    fun addTarefaTemplate(descricao: String) {
        val turnoId = _uiState.value.selectedTurnoId ?: return
        val exists = _uiState.value.tarefas.any { it.descricao == descricao }
        if (exists) return
        viewModelScope.launch {
            repo.updateTarefa(
                TarefaProgramadaEntity(
                    turnoId = turnoId,
                    categoria = "Fluxo de placas",
                    descricao = descricao,
                    quantidade = 1,
                    frequencia = 1,
                    tempoBaseMin = 30.0,
                    modoCalculo = "MULTIPLICAR",
                    tempoTotalMin = 30.0
                )
            )
        }
    }

    fun removeTarefa(tarefaId: Int) {
        viewModelScope.launch { repo.deleteTarefa(tarefaId) }
    }

    fun toggleRecurso(recursoId: Int, selected: Boolean) {
        val turnoId = _uiState.value.selectedTurnoId ?: return
        val ids = _uiState.value.recursosSelecionados.map { it.id }.toMutableSet()
        if (selected) ids += recursoId else ids -= recursoId
        viewModelScope.launch { repo.setRecursosTurno(turnoId, ids.toList()) }
    }

    fun toggleFuncionario(funcaoId: Int, funcionarioId: Int, selected: Boolean) {
        val turnoId = _uiState.value.selectedTurnoId ?: return
        val atual = _uiState.value.alocacoes.find { it.funcaoId == funcaoId }
        val list = parseCsv(atual?.selecionadosCsv).toMutableSet()
        if (selected) list += funcionarioId else list -= funcionarioId

        viewModelScope.launch {
            repo.saveAlocacao(turnoId, funcaoId, list.toList(), atual?.funcionarioOutraLetraId)
        }
    }

    fun setFuncionarioOutraLetra(funcaoId: Int, funcionarioId: Int?) {
        val turnoId = _uiState.value.selectedTurnoId ?: return
        val atual = _uiState.value.alocacoes.find { it.funcaoId == funcaoId }
        viewModelScope.launch {
            repo.saveAlocacao(turnoId, funcaoId, parseCsv(atual?.selecionadosCsv), funcionarioId)
        }
    }

    fun addAlocacaoAtividade(tarefaId: Int, equipamentoId: Int?, maoObraId: Int?) {
        val turnoId = _uiState.value.selectedTurnoId ?: return
        viewModelScope.launch {
            repo.saveAlocacaoAtividade(turnoId, tarefaId, maoObraId, equipamentoId)
        }
    }

    fun removeAlocacaoAtividade(id: Int) {
        viewModelScope.launch { repo.deleteAlocacaoAtividade(id) }
    }

    fun addOcorrencia(tipo: String, descricao: String, equipamento: String?, maoObra: String?) {
        val turno = _uiState.value.currentTurno ?: return
        if (descricao.isBlank()) return
        viewModelScope.launch {
            repo.addOcorrencia(
                turnoId = turno.id,
                tipo = tipo,
                descricao = descricao.trim(),
                equipamento = equipamento,
                maoObra = maoObra,
                data = turno.data,
                janela = turno.janela,
                supervisorId = turno.supervisorId
            )
        }
    }

    fun importExcel(uri: Uri) {
        val turnoId = _uiState.value.selectedTurnoId ?: return
        viewModelScope.launch {
            val msg = repo.importExcel(turnoId, uri)
            _uiState.value = _uiState.value.copy(message = msg)
        }
    }

    fun finalizarTurno() {
        val state = _uiState.value
        val turno = state.currentTurno
        if (turno == null || state.selectedSupervisorId == null || state.tarefas.isEmpty()) {
            _uiState.value = _uiState.value.copy(message = "Preencha supervisor, turno/data e programacao antes de finalizar")
            return
        }

        val programadoTotal = state.tarefas.sumOf { it.quantidade }
        val atendidoTotal = state.tarefas.sumOf { it.quantidadeExecutada }

        viewModelScope.launch {
            repo.updateTurno(turno.copy(status = "FINALIZADO"))
            repo.saveTurnoResumo(
                TurnoResumoEntity(
                    turnoId = turno.id,
                    data = turno.data,
                    turno = turno.janela,
                    supervisorId = turno.supervisorId,
                    capacidadePct = (state.metrics.percentualUso * 100.0).coerceAtLeast(0.0),
                    acimaDaCapacidadePct = (state.metrics.percentualAcima * 100.0).coerceAtLeast(0.0),
                    programadoTotal = programadoTotal,
                    atendidoTotal = atendidoTotal,
                    cumpriuProgramacaoBool = atendidoTotal >= programadoTotal
                )
            )
            _uiState.value = _uiState.value.copy(message = "Turno finalizado")
        }
    }

    fun recursoIndisponivelPorRegra(recurso: RecursoOperacionalEntity): Boolean {
        if (!recurso.admFlag) return false
        val state = _uiState.value
        val turnoNoite = state.selectedJanela == "19x07"
        val date = runCatching { LocalDate.parse(state.selectedDate) }.getOrNull() ?: return turnoNoite
        return turnoNoite || isWeekend(date) || isHoliday(date)
    }

    fun saveSupervisor(nome: String) {
        if (nome.isBlank()) return
        viewModelScope.launch {
            repo.saveSupervisor(id = null, nome = nome.trim())
        }
    }

    private fun parseCsv(csv: String?): List<Int> {
        if (csv.isNullOrBlank()) return emptyList()
        return csv.split(",").mapNotNull { it.trim().toIntOrNull() }.distinct()
    }

    private fun statusFromExecucao(executada: Int, programada: Int): String {
        return when {
            executada <= 0 -> "NAO_INICIADO"
            executada < programada -> "PARCIAL"
            else -> "CONCLUIDO"
        }
    }

    private fun isWeekend(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    }

    private fun isHoliday(date: LocalDate): Boolean {
        val fixed = setOf("01-01", "04-21", "05-01", "09-07", "10-12", "11-02", "11-15", "12-25")
        return fixed.contains("%02d-%02d".format(date.monthValue, date.dayOfMonth))
    }
}

class MainViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel() as T
    }
}

