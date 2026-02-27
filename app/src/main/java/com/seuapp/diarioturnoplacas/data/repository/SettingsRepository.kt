package com.seuapp.diarioturnoplacas.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "diario_turno_settings")

data class AppSettings(
    val minutosTurno: Int = 720,
    val ultimoSupervisorId: Int? = null,
    val ultimaData: String? = null,
    val ultimaJanela: String = "07x19"
)

class SettingsRepository(private val context: Context) {
    private val minutosKey = intPreferencesKey("minutos_turno")
    private val supKey = intPreferencesKey("ultimo_supervisor")
    private val dataKey = stringPreferencesKey("ultima_data")
    private val janelaKey = stringPreferencesKey("ultima_janela")

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            minutosTurno = prefs[minutosKey] ?: 720,
            ultimoSupervisorId = prefs[supKey],
            ultimaData = prefs[dataKey],
            ultimaJanela = prefs[janelaKey] ?: "07x19"
        )
    }

    suspend fun setMinutosTurno(valor: Int) {
        context.dataStore.edit { prefs ->
            prefs[minutosKey] = valor
        }
    }

    suspend fun saveContext(supervisorId: Int?, data: String, janela: String) {
        context.dataStore.edit { prefs ->
            if (supervisorId != null) {
                prefs[supKey] = supervisorId
            }
            prefs[dataKey] = data
            prefs[janelaKey] = janela
        }
    }
}
