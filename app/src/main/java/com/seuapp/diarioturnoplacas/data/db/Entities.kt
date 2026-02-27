package com.seuapp.diarioturnoplacas.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supervisor")
data class SupervisorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String
)

@Entity(tableName = "turno")
data class TurnoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val data: String,
    val janela: String,
    val supervisorId: Int,
    val status: String
)

@Entity(tableName = "funcao_mo")
data class FuncaoMOEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nomeFuncao: String,
    val efetivoPrevisto: Int,
    val fatorProdutividade: Double = 1.0
)

@Entity(tableName = "funcionario")
data class FuncionarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val funcaoId: Int,
    val letra: String
)

@Entity(tableName = "alocacao_mo", primaryKeys = ["turnoId", "funcaoId"])
data class AlocacaoMOEntity(
    val turnoId: Int,
    val funcaoId: Int,
    val selecionadosCsv: String,
    val funcionarioOutraLetraId: Int? = null,
    val observacao: String = ""
)

@Entity(tableName = "recurso_operacional")
data class RecursoOperacionalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tag: String,
    val tipo: String,
    val admFlag: Boolean = false
)

@Entity(tableName = "turno_recurso", primaryKeys = ["turnoId", "recursoId"])
data class TurnoRecursoCrossRef(
    val turnoId: Int,
    val recursoId: Int
)

@Entity(tableName = "tarefa_programada")
data class TarefaProgramadaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val turnoId: Int,
    val categoria: String,
    val descricao: String,
    val quantidade: Int,
    val frequencia: Int,
    val tempoBaseMin: Double,
    val modoCalculo: String,
    val tempoTotalMin: Double,
    val recursoSugestao: String? = null,
    val statusExecucao: String = "NAO_INICIADO",
    val quantidadeExecutada: Int = 0,
    val observacaoTurno: String = ""
)

@Entity(tableName = "alocacao_atividade")
data class AlocacaoAtividadeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val turnoId: Int,
    val tarefaId: Int,
    val maoObraId: Int? = null,
    val equipamentoId: Int? = null
)

@Entity(tableName = "ocorrencia_turno")
data class OcorrenciaTurnoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val turnoId: Int,
    val tipo: String,
    val descricao: String,
    val equipamentoRelacionado: String? = null,
    val maoObraRelacionada: String? = null,
    val horario: String,
    val data: String,
    val janela: String,
    val supervisorId: Int
)

@Entity(tableName = "turno_resumo")
data class TurnoResumoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val turnoId: Int,
    val data: String,
    val turno: String,
    val supervisorId: Int,
    val capacidadePct: Double,
    val acimaDaCapacidadePct: Double,
    val programadoTotal: Int,
    val atendidoTotal: Int,
    val cumpriuProgramacaoBool: Boolean
)
