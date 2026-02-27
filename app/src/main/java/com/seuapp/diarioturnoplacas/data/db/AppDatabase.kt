package com.seuapp.diarioturnoplacas.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SupervisorEntity::class,
        TurnoEntity::class,
        FuncaoMOEntity::class,
        FuncionarioEntity::class,
        AlocacaoMOEntity::class,
        RecursoOperacionalEntity::class,
        TurnoRecursoCrossRef::class,
        TarefaProgramadaEntity::class,
        AlocacaoAtividadeEntity::class,
        OcorrenciaTurnoEntity::class,
        TurnoResumoEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "diario_turno_placas.db"
            ).fallbackToDestructiveMigration().build()
        }
    }
}
