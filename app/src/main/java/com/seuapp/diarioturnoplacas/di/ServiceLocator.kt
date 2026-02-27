package com.seuapp.diarioturnoplacas.di

import android.content.Context
import com.seuapp.diarioturnoplacas.data.db.AppDatabase
import com.seuapp.diarioturnoplacas.data.importer.ExcelImporter
import com.seuapp.diarioturnoplacas.data.repository.AppRepository
import com.seuapp.diarioturnoplacas.data.repository.SettingsRepository
import com.seuapp.diarioturnoplacas.domain.usecase.CapacityCalculator

object ServiceLocator {
    @Volatile
    private var initialized = false

    lateinit var appRepository: AppRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var capacityCalculator: CapacityCalculator
        private set

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val db = AppDatabase.create(context)
            appRepository = AppRepository(db.appDao(), ExcelImporter(context.contentResolver))
            settingsRepository = SettingsRepository(context)
            capacityCalculator = CapacityCalculator()
            initialized = true
        }
    }
}
