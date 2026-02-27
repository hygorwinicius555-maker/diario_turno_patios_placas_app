package com.seuapp.diarioturnoplacas.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seuapp.diarioturnoplacas.navigation.appTabs
import com.seuapp.diarioturnoplacas.ui.screens.AppUiState
import com.seuapp.diarioturnoplacas.ui.screens.CapacidadeScreen
import com.seuapp.diarioturnoplacas.ui.screens.FinalizacaoScreen
import com.seuapp.diarioturnoplacas.ui.screens.MainViewModel
import com.seuapp.diarioturnoplacas.ui.screens.MainViewModelFactory
import com.seuapp.diarioturnoplacas.ui.screens.OcorrenciasScreen
import com.seuapp.diarioturnoplacas.ui.screens.ProgramacaoScreen
import com.seuapp.diarioturnoplacas.ui.screens.RecursosScreen

@Composable
fun AppScaffold() {
    val vm: MainViewModel = viewModel(factory = MainViewModelFactory())
    val uiState by vm.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    var currentRoute by remember { mutableStateOf(appTabs.first().route) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFE6E7EA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AppHeader()
            DashboardGrid(
                currentRoute = currentRoute,
                onRouteChange = { currentRoute = it }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp)
            ) {
                AppContent(route = currentRoute, uiState = uiState, vm = vm)
            }
        }
    }
}

@Composable
private fun AppHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2F6695))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
            Row {
                Text(
                    text = "Diario",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Turno",
                    color = Color.White,
                    fontStyle = FontStyle.Italic
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.NotificationsNone,
                    contentDescription = "Notificacoes",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun DashboardGrid(currentRoute: String, onRouteChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = appTabs.chunked(3)
        rows.forEach { rowTabs ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTabs.forEach { tab ->
                    val selected = tab.route == currentRoute
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .clickable { onRouteChange(tab.route) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) Color(0xFFD6E6F7) else Color(0xFFF2F2F2)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = Color(0xFF2F6695),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = tab.label.uppercase(),
                                color = Color(0xFF6B7179),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                repeat(3 - rowTabs.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AppContent(route: String, uiState: AppUiState, vm: MainViewModel) {
    androidx.compose.animation.Crossfade(
        targetState = route,
        animationSpec = tween(durationMillis = 220),
        label = "tabs"
    ) { current ->
        when (current) {
            "recursos" -> RecursosScreen(uiState, vm)
            "programacao" -> ProgramacaoScreen(uiState, vm)
            "capacidade" -> CapacidadeScreen(uiState)
            "finalizacao" -> FinalizacaoScreen(uiState, vm)
            else -> OcorrenciasScreen(uiState, vm)
        }
    }
}
