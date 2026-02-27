package com.seuapp.diarioturnoplacas.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query(
        """
        SELECT
            t.id AS turnoId,
            t.data AS data,
            t.janela AS janela,
            t.status AS status,
            s.nome AS supervisorNome,
            (SELECT SUM(tp.quantidade) FROM tarefa_programada tp WHERE tp.turnoId = t.id) AS programado,
            (SELECT SUM(tp.quantidadeExecutada) FROM tarefa_programada tp WHERE tp.turnoId = t.id) AS executado,
            tr.capacidadePct AS capacidadePct,
            tr.acimaDaCapacidadePct AS acimaCapacidadePct,
            (SELECT COUNT(*) FROM ocorrencia_turno o WHERE o.turnoId = t.id) AS ocorrencias,
            (SELECT o.descricao FROM ocorrencia_turno o WHERE o.turnoId = t.id ORDER BY o.id DESC LIMIT 1) AS ultimoEvento
        FROM turno t
        LEFT JOIN supervisor s ON s.id = t.supervisorId
        LEFT JOIN turno_resumo tr ON tr.turnoId = t.id
        ORDER BY t.id DESC
        """
    )
    fun historicoTurnosFlow(): Flow<List<TurnoHistoricoRow>>

    @Query("SELECT * FROM supervisor ORDER BY nome")
    fun supervisorsFlow(): Flow<List<SupervisorEntity>>

    @Upsert
    suspend fun upsertSupervisor(supervisor: SupervisorEntity)

    @Query("DELETE FROM supervisor WHERE id = :id")
    suspend fun deleteSupervisorById(id: Int)

    @Query("SELECT COUNT(*) FROM supervisor")
    suspend fun supervisorsCount(): Int

    @Query("SELECT * FROM turno ORDER BY id DESC")
    fun turnosFlow(): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turno WHERE id = :turnoId LIMIT 1")
    fun turnoByIdFlow(turnoId: Int): Flow<TurnoEntity?>

    @Insert
    suspend fun insertTurno(turno: TurnoEntity): Long

    @Update
    suspend fun updateTurno(turno: TurnoEntity)

    @Query("SELECT COUNT(*) FROM turno")
    suspend fun turnosCount(): Int

    @Query("SELECT * FROM funcao_mo ORDER BY nomeFuncao")
    fun funcoesFlow(): Flow<List<FuncaoMOEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuncoes(funcoes: List<FuncaoMOEntity>)

    @Upsert
    suspend fun upsertFuncao(funcao: FuncaoMOEntity)

    @Query("DELETE FROM funcao_mo WHERE id = :id")
    suspend fun deleteFuncaoById(id: Int)

    @Query("SELECT COUNT(*) FROM funcionario WHERE funcaoId = :funcaoId")
    suspend fun countFuncionariosByFuncao(funcaoId: Int): Int

    @Query("SELECT COUNT(*) FROM funcao_mo")
    suspend fun funcoesCount(): Int

    @Query("SELECT * FROM funcionario ORDER BY nome")
    fun funcionariosFlow(): Flow<List<FuncionarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuncionarios(funcionarios: List<FuncionarioEntity>)

    @Upsert
    suspend fun upsertFuncionario(funcionario: FuncionarioEntity)

    @Query("DELETE FROM funcionario WHERE id = :id")
    suspend fun deleteFuncionarioById(id: Int)

    @Query("SELECT COUNT(*) FROM funcionario")
    suspend fun funcionariosCount(): Int

    @Query("SELECT * FROM alocacao_mo WHERE turnoId = :turnoId")
    fun alocacoesTurnoFlow(turnoId: Int): Flow<List<AlocacaoMOEntity>>

    @Upsert
    suspend fun upsertAlocacao(alocacao: AlocacaoMOEntity)

    @Query("SELECT * FROM recurso_operacional ORDER BY tag")
    fun recursosFlow(): Flow<List<RecursoOperacionalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecursos(recursos: List<RecursoOperacionalEntity>)

    @Upsert
    suspend fun upsertRecurso(recurso: RecursoOperacionalEntity)

    @Query("DELETE FROM recurso_operacional WHERE id = :id")
    suspend fun deleteRecursoById(id: Int)

    @Query("SELECT COUNT(*) FROM recurso_operacional")
    suspend fun recursosCount(): Int

    @Query(
        """
        SELECT r.* FROM recurso_operacional r
        INNER JOIN turno_recurso tr ON r.id = tr.recursoId
        WHERE tr.turnoId = :turnoId
        ORDER BY r.tag
        """
    )
    fun recursosSelecionadosFlow(turnoId: Int): Flow<List<RecursoOperacionalEntity>>

    @Query("DELETE FROM turno_recurso WHERE turnoId = :turnoId")
    suspend fun deleteTurnoRecursos(turnoId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurnoRecursos(refs: List<TurnoRecursoCrossRef>)

    @Query("SELECT * FROM tarefa_programada WHERE turnoId = :turnoId ORDER BY categoria, descricao")
    fun tarefasTurnoFlow(turnoId: Int): Flow<List<TarefaProgramadaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarefas(tarefas: List<TarefaProgramadaEntity>)

    @Upsert
    suspend fun upsertTarefa(tarefa: TarefaProgramadaEntity)

    @Query("DELETE FROM tarefa_programada WHERE id = :id")
    suspend fun deleteTarefaById(id: Int)

    @Query("DELETE FROM tarefa_programada WHERE turnoId = :turnoId")
    suspend fun deleteTarefasByTurno(turnoId: Int)

    @Query("SELECT COUNT(*) FROM tarefa_programada")
    suspend fun tarefasCount(): Int

    @Query("SELECT * FROM alocacao_atividade WHERE turnoId = :turnoId ORDER BY id DESC")
    fun alocacoesAtividadeFlow(turnoId: Int): Flow<List<AlocacaoAtividadeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlocacaoAtividade(item: AlocacaoAtividadeEntity)

    @Query("DELETE FROM alocacao_atividade WHERE id = :id")
    suspend fun deleteAlocacaoAtividade(id: Int)

    @Query("DELETE FROM alocacao_atividade WHERE turnoId = :turnoId")
    suspend fun deleteAlocacaoAtividadeByTurno(turnoId: Int)

    @Query("SELECT * FROM ocorrencia_turno WHERE turnoId = :turnoId ORDER BY id DESC")
    fun ocorrenciasTurnoFlow(turnoId: Int): Flow<List<OcorrenciaTurnoEntity>>

    @Query("SELECT * FROM ocorrencia_turno ORDER BY id DESC")
    fun ocorrenciasFlow(): Flow<List<OcorrenciaTurnoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOcorrencia(ocorrencia: OcorrenciaTurnoEntity)

    @Query("SELECT * FROM turno_resumo WHERE turnoId = :turnoId LIMIT 1")
    suspend fun resumoByTurno(turnoId: Int): TurnoResumoEntity?

    @Upsert
    suspend fun upsertTurnoResumo(resumo: TurnoResumoEntity)

    @Query("SELECT * FROM supervisor ORDER BY id")
    suspend fun supervisorsNow(): List<SupervisorEntity>

    @Query("SELECT * FROM funcao_mo ORDER BY id")
    suspend fun funcoesNow(): List<FuncaoMOEntity>
}

