package com.seuapp.diarioturnoplacas.data.repository

import android.net.Uri
import com.seuapp.diarioturnoplacas.data.db.AlocacaoAtividadeEntity
import com.seuapp.diarioturnoplacas.data.db.AlocacaoMOEntity
import com.seuapp.diarioturnoplacas.data.db.AppDao
import com.seuapp.diarioturnoplacas.data.db.FuncaoMOEntity
import com.seuapp.diarioturnoplacas.data.db.FuncionarioEntity
import com.seuapp.diarioturnoplacas.data.db.OcorrenciaTurnoEntity
import com.seuapp.diarioturnoplacas.data.db.RecursoOperacionalEntity
import com.seuapp.diarioturnoplacas.data.db.SupervisorEntity
import com.seuapp.diarioturnoplacas.data.db.TarefaProgramadaEntity
import com.seuapp.diarioturnoplacas.data.db.TurnoEntity
import com.seuapp.diarioturnoplacas.data.db.TurnoHistoricoRow
import com.seuapp.diarioturnoplacas.data.db.TurnoRecursoCrossRef
import com.seuapp.diarioturnoplacas.data.db.TurnoResumoEntity
import com.seuapp.diarioturnoplacas.data.importer.ExcelImporter
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

class AppRepository(
    private val dao: AppDao,
    private val excelImporter: ExcelImporter
) {
    fun historicoTurnosFlow(): Flow<List<TurnoHistoricoRow>> = dao.historicoTurnosFlow()
    fun supervisorsFlow(): Flow<List<SupervisorEntity>> = dao.supervisorsFlow()
    fun turnosFlow(): Flow<List<TurnoEntity>> = dao.turnosFlow()
    fun funcoesFlow(): Flow<List<FuncaoMOEntity>> = dao.funcoesFlow()
    fun funcionariosFlow(): Flow<List<FuncionarioEntity>> = dao.funcionariosFlow()
    fun recursosFlow(): Flow<List<RecursoOperacionalEntity>> = dao.recursosFlow()
    fun tarefasTurnoFlow(turnoId: Int): Flow<List<TarefaProgramadaEntity>> = dao.tarefasTurnoFlow(turnoId)
    fun alocacoesTurnoFlow(turnoId: Int): Flow<List<AlocacaoMOEntity>> = dao.alocacoesTurnoFlow(turnoId)
    fun recursosSelecionadosFlow(turnoId: Int): Flow<List<RecursoOperacionalEntity>> = dao.recursosSelecionadosFlow(turnoId)
    fun ocorrenciasTurnoFlow(turnoId: Int): Flow<List<OcorrenciaTurnoEntity>> = dao.ocorrenciasTurnoFlow(turnoId)
    fun ocorrenciasFlow(): Flow<List<OcorrenciaTurnoEntity>> = dao.ocorrenciasFlow()
    fun alocacoesAtividadeFlow(turnoId: Int): Flow<List<AlocacaoAtividadeEntity>> = dao.alocacoesAtividadeFlow(turnoId)

    suspend fun seedIfNeeded() {
        val recursosImport = excelImporter.importRecursosFromAssets("dRecursos/Recurso App.xlsx")

        if (dao.supervisorsCount() == 0) {
            val supervisors = recursosImport?.supervisors?.takeIf { it.isNotEmpty() }
                ?: listOf("Carlos Lima", "Ana Souza", "Rafael Nunes")
            supervisors.forEach { dao.upsertSupervisor(SupervisorEntity(nome = it)) }
        }

        if (dao.funcoesCount() == 0) {
            val funcoesFromImport = recursosImport?.funcoesPrevisto?.entries
                ?.map { FuncaoMOEntity(nomeFuncao = it.key, efetivoPrevisto = it.value.coerceAtLeast(1)) }
                ?.sortedBy { it.nomeFuncao }
                .orEmpty()

            if (funcoesFromImport.isNotEmpty()) {
                dao.insertFuncoes(funcoesFromImport)
            } else {
                dao.insertFuncoes(
                    listOf(
                        FuncaoMOEntity(nomeFuncao = "Controlador", efetivoPrevisto = 2),
                        FuncaoMOEntity(nomeFuncao = "Operador PA", efetivoPrevisto = 3),
                        FuncaoMOEntity(nomeFuncao = "Conferente", efetivoPrevisto = 2),
                        FuncaoMOEntity(nomeFuncao = "Motorista Patio", efetivoPrevisto = 4)
                    )
                )
            }
        }

        if (dao.funcionariosCount() == 0) {
            val funcoes = dao.funcoesNow()
            val funcoesByNormalized = funcoes.associateBy { normalize(it.nomeFuncao) }

            val funcionariosFromImport = recursosImport?.funcionarios
                ?.mapNotNull { imported ->
                    val funcaoId = funcoesByNormalized[normalize(imported.funcao)]?.id ?: return@mapNotNull null
                    FuncionarioEntity(nome = imported.nome, funcaoId = funcaoId, letra = imported.letra)
                }
                .orEmpty()

            if (funcionariosFromImport.isNotEmpty()) {
                dao.insertFuncionarios(funcionariosFromImport)
            } else {
                dao.insertFuncionarios(
                    listOf(
                        FuncionarioEntity(nome = "Joao A", funcaoId = 1, letra = "A"),
                        FuncionarioEntity(nome = "Pedro B", funcaoId = 1, letra = "B"),
                        FuncionarioEntity(nome = "Marcos C", funcaoId = 1, letra = "C"),
                        FuncionarioEntity(nome = "Luis A", funcaoId = 2, letra = "A"),
                        FuncionarioEntity(nome = "Tiago B", funcaoId = 2, letra = "B"),
                        FuncionarioEntity(nome = "Andre C", funcaoId = 2, letra = "C"),
                        FuncionarioEntity(nome = "Diego D", funcaoId = 2, letra = "D"),
                        FuncionarioEntity(nome = "Paulo A", funcaoId = 3, letra = "A"),
                        FuncionarioEntity(nome = "Renan B", funcaoId = 3, letra = "B"),
                        FuncionarioEntity(nome = "Fabio C", funcaoId = 3, letra = "C"),
                        FuncionarioEntity(nome = "Ivo A", funcaoId = 4, letra = "A"),
                        FuncionarioEntity(nome = "Vitor B", funcaoId = 4, letra = "B"),
                        FuncionarioEntity(nome = "Bruno C", funcaoId = 4, letra = "C"),
                        FuncionarioEntity(nome = "Caio D", funcaoId = 4, letra = "D")
                    )
                )
            }
        }

        if (dao.recursosCount() == 0) {
            val recursosFromImport = recursosImport?.recursos
                ?.map {
                    RecursoOperacionalEntity(
                        tag = it.tag,
                        tipo = it.categoria,
                        admFlag = it.admFlag
                    )
                }
                .orEmpty()

            if (recursosFromImport.isNotEmpty()) {
                dao.insertRecursos(recursosFromImport)
            } else {
                dao.insertRecursos(
                    listOf(
                        RecursoOperacionalEntity(tag = "RS-01", tipo = "Reach Stacker"),
                        RecursoOperacionalEntity(tag = "EMP-02", tipo = "Empilhadeira"),
                        RecursoOperacionalEntity(tag = "MOTO-01", tipo = "Motoniveladora", admFlag = true),
                        RecursoOperacionalEntity(tag = "ROLO-01", tipo = "Rolo Compactador", admFlag = true),
                        RecursoOperacionalEntity(tag = "CAM-05", tipo = "Caminhao Interno")
                    )
                )
            }
        }

        if (dao.turnosCount() == 0) {
            val today = LocalDate.now().toString()
            val firstSupervisorId = dao.supervisorsNow().firstOrNull()?.id ?: 1
            val turnoId = dao.insertTurno(
                TurnoEntity(
                    data = today,
                    janela = "07x19",
                    supervisorId = firstSupervisorId,
                    status = "ABERTO"
                )
            ).toInt()

            val tarefasFromImport = excelImporter.importTarefasFromAssets(turnoId, "dRecursos/Tarefas App 2.0.xlsx")
            if (tarefasFromImport.isNotEmpty()) {
                dao.insertTarefas(tarefasFromImport)
            } else {
                dao.insertTarefas(
                    listOf(
                        TarefaProgramadaEntity(
                            turnoId = turnoId,
                            categoria = "Carregamento",
                            descricao = "Carregar bobinas area 1",
                            quantidade = 8,
                            frequencia = 2,
                            tempoBaseMin = 18.0,
                            modoCalculo = "MULTIPLICAR",
                            tempoTotalMin = 288.0,
                            recursoSugestao = "RS-01"
                        ),
                        TarefaProgramadaEntity(
                            turnoId = turnoId,
                            categoria = "Recebimento",
                            descricao = "Receber placas patio norte",
                            quantidade = 6,
                            frequencia = 1,
                            tempoBaseMin = 25.0,
                            modoCalculo = "MULTIPLICAR",
                            tempoTotalMin = 150.0,
                            recursoSugestao = "EMP-02"
                        )
                    )
                )
            }

            dao.insertTurnoRecursos(listOf(TurnoRecursoCrossRef(turnoId, 1), TurnoRecursoCrossRef(turnoId, 2)))
            dao.upsertAlocacao(AlocacaoMOEntity(turnoId = turnoId, funcaoId = 1, selecionadosCsv = "1,2"))
            dao.upsertAlocacao(AlocacaoMOEntity(turnoId = turnoId, funcaoId = 2, selecionadosCsv = "4,5,6"))
        }
    }

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace("á", "a")
            .replace("à", "a")
            .replace("â", "a")
            .replace("ã", "a")
            .replace("é", "e")
            .replace("ê", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ô", "o")
            .replace("õ", "o")
            .replace("ú", "u")
            .replace("ç", "c")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    suspend fun createTurno(data: String, janela: String, supervisorId: Int): Int {
        return dao.insertTurno(
            TurnoEntity(
                data = data,
                janela = janela,
                supervisorId = supervisorId,
                status = "ABERTO"
            )
        ).toInt()
    }

    suspend fun updateTurno(turno: TurnoEntity) = dao.updateTurno(turno)

    suspend fun saveSupervisor(id: Int?, nome: String) = dao.upsertSupervisor(SupervisorEntity(id = id ?: 0, nome = nome))

    suspend fun saveFuncao(id: Int?, nome: String, efetivoPrevisto: Int, fator: Double) {
        dao.upsertFuncao(
            FuncaoMOEntity(
                id = id ?: 0,
                nomeFuncao = nome,
                efetivoPrevisto = efetivoPrevisto,
                fatorProdutividade = fator
            )
        )
    }

    suspend fun saveFuncionario(id: Int?, nome: String, funcaoId: Int, letra: String) {
        dao.upsertFuncionario(
            FuncionarioEntity(id = id ?: 0, nome = nome, funcaoId = funcaoId, letra = letra)
        )
    }

    suspend fun saveRecurso(id: Int?, tag: String, tipo: String, admFlag: Boolean) {
        dao.upsertRecurso(
            RecursoOperacionalEntity(id = id ?: 0, tag = tag, tipo = tipo, admFlag = admFlag)
        )
    }

    suspend fun deleteSupervisor(id: Int) = dao.deleteSupervisorById(id)
    suspend fun deleteFuncionario(id: Int) = dao.deleteFuncionarioById(id)
    suspend fun deleteRecurso(id: Int) = dao.deleteRecursoById(id)

    suspend fun deleteFuncao(id: Int): Boolean {
        val count = dao.countFuncionariosByFuncao(id)
        if (count > 0) return false
        dao.deleteFuncaoById(id)
        return true
    }

    suspend fun saveAlocacao(turnoId: Int, funcaoId: Int, selecionados: List<Int>, outraLetraId: Int?) {
        dao.upsertAlocacao(
            AlocacaoMOEntity(
                turnoId = turnoId,
                funcaoId = funcaoId,
                selecionadosCsv = selecionados.distinct().joinToString(","),
                funcionarioOutraLetraId = outraLetraId
            )
        )
    }

    suspend fun setRecursosTurno(turnoId: Int, recursoIds: List<Int>) {
        dao.deleteTurnoRecursos(turnoId)
        dao.insertTurnoRecursos(recursoIds.distinct().map { TurnoRecursoCrossRef(turnoId, it) })
    }

    suspend fun saveAlocacaoAtividade(turnoId: Int, tarefaId: Int, maoObraId: Int?, equipamentoId: Int?) {
        dao.insertAlocacaoAtividade(
            AlocacaoAtividadeEntity(
                turnoId = turnoId,
                tarefaId = tarefaId,
                maoObraId = maoObraId,
                equipamentoId = equipamentoId
            )
        )
    }

    suspend fun deleteAlocacaoAtividade(id: Int) = dao.deleteAlocacaoAtividade(id)

    suspend fun updateTarefa(tarefa: TarefaProgramadaEntity) = dao.upsertTarefa(tarefa)
    suspend fun deleteTarefa(id: Int) = dao.deleteTarefaById(id)

    suspend fun addOcorrencia(
        turnoId: Int,
        tipo: String,
        descricao: String,
        equipamento: String?,
        maoObra: String?,
        data: String,
        janela: String,
        supervisorId: Int
    ) {
        dao.insertOcorrencia(
            OcorrenciaTurnoEntity(
                turnoId = turnoId,
                tipo = tipo,
                descricao = descricao,
                equipamentoRelacionado = equipamento,
                maoObraRelacionada = maoObra,
                horario = LocalTime.now().withSecond(0).withNano(0).toString(),
                data = data,
                janela = janela,
                supervisorId = supervisorId
            )
        )
    }

    suspend fun saveTurnoResumo(resumo: TurnoResumoEntity) {
        dao.upsertTurnoResumo(resumo)
    }

    suspend fun importExcel(turnoId: Int, uri: Uri): String {
        val (tarefas, result) = excelImporter.importTarefas(turnoId, uri)
        if (tarefas.isNotEmpty()) {
            dao.deleteTarefasByTurno(turnoId)
            dao.insertTarefas(tarefas)
        }
        return "Importadas: ${result.imported}. Erros: ${result.errors.size}"
    }
}
