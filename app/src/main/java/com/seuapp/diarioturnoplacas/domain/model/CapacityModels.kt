package com.seuapp.diarioturnoplacas.domain.model

data class CapacidadePorAtividade(
    val tarefaId: Int,
    val descricao: String,
    val alocacoes: Int,
    val demandaMin: Double,
    val capacidadeMin: Double,
    val acimaCapacidade: Boolean
)

data class CapacityMetrics(
    val demandaMin: Double,
    val capacidadeMin: Double,
    val percentualUso: Double,
    val percentualAcima: Double,
    val acimaCapacidade: Boolean,
    val causa: String,
    val porAtividade: List<CapacidadePorAtividade>
)
