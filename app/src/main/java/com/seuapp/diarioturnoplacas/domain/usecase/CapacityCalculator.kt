package com.seuapp.diarioturnoplacas.domain.usecase

import com.seuapp.diarioturnoplacas.data.db.AlocacaoAtividadeEntity
import com.seuapp.diarioturnoplacas.data.db.TarefaProgramadaEntity
import com.seuapp.diarioturnoplacas.domain.model.CapacidadePorAtividade
import com.seuapp.diarioturnoplacas.domain.model.CapacityMetrics
import kotlin.math.max

class CapacityCalculator {
    fun calculate(
        tarefas: List<TarefaProgramadaEntity>,
        alocacoesAtividade: List<AlocacaoAtividadeEntity>,
        minutosDisponiveisTurno: Int
    ): CapacityMetrics {
        val demanda = tarefas.sumOf { it.tempoTotalMin }
        val byTask = alocacoesAtividade.groupBy { it.tarefaId }

        val porAtividade = tarefas.map { tarefa ->
            val taskAlloc = byTask[tarefa.id].orEmpty()
            val capacidade = taskAlloc.size * minutosDisponiveisTurno * 0.6
            CapacidadePorAtividade(
                tarefaId = tarefa.id,
                descricao = tarefa.descricao,
                alocacoes = taskAlloc.size,
                demandaMin = tarefa.tempoTotalMin,
                capacidadeMin = capacidade,
                acimaCapacidade = tarefa.tempoTotalMin > max(1.0, capacidade)
            )
        }

        val capacidadeTotal = porAtividade.sumOf { it.capacidadeMin }
        val uso = if (capacidadeTotal <= 0.0) 0.0 else demanda / capacidadeTotal
        val acima = max(0.0, uso - 1.0)
        val acimaCapacidade = demanda > capacidadeTotal && capacidadeTotal > 0.0
        val causa = when {
            !acimaCapacidade -> "DENTRO DA CAPACIDADE"
            porAtividade.any { it.alocacoes == 0 } -> "ACIMA POR ATIVIDADES SEM ALOCACAO"
            else -> "ACIMA POR DEMANDA/PRODUTIVIDADE"
        }

        return CapacityMetrics(
            demandaMin = demanda,
            capacidadeMin = capacidadeTotal,
            percentualUso = uso,
            percentualAcima = acima,
            acimaCapacidade = acimaCapacidade,
            causa = causa,
            porAtividade = porAtividade
        )
    }
}
