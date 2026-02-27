package com.seuapp.diarioturnoplacas.data.db

data class TurnoHistoricoRow(
    val turnoId: Int,
    val data: String,
    val janela: String,
    val status: String,
    val supervisorNome: String?,
    val programado: Int?,
    val executado: Int?,
    val capacidadePct: Double?,
    val acimaCapacidadePct: Double?,
    val ocorrencias: Int,
    val ultimoEvento: String?
)
