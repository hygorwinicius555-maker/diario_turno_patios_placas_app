package com.seuapp.diarioturnoplacas.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Report
import androidx.compose.ui.graphics.vector.ImageVector

data class AppTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val appTabs = listOf(
    AppTab("recursos", "Recursos", Icons.Filled.People),
    AppTab("programacao", "Programacao", Icons.Filled.Checklist),
    AppTab("capacidade", "Capacidade", Icons.Filled.Analytics),
    AppTab("finalizacao", "Finalizacao", Icons.Filled.DoneAll),
    AppTab("ocorrencias", "Ocorrencias", Icons.Filled.Report)
)
