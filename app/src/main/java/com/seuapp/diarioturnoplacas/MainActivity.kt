package com.seuapp.diarioturnoplacas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.seuapp.diarioturnoplacas.di.ServiceLocator
import com.seuapp.diarioturnoplacas.ui.AppScaffold
import com.seuapp.diarioturnoplacas.ui.theme.DiarioTurnoPlacasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(applicationContext)

        setContent {
            DiarioTurnoPlacasTheme {
                AppScaffold()
            }
        }
    }
}
