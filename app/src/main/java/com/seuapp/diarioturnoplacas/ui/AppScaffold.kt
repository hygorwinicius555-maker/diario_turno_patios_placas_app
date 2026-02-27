package com.seuapp.diarioturnoplacas.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
        bottomBar = {
            AppBottomBar(currentRoute = currentRoute, onRouteChange = { currentRoute = it })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AppContent(route = currentRoute, uiState = uiState, vm = vm)
        }
    }
}

@Composable
private fun AppBottomBar(currentRoute: String, onRouteChange: (String) -> Unit) {
    AnimatedVisibility(visible = true) {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            appTabs.forEach { tab ->
                NavigationBarItem(
                    selected = currentRoute == tab.route,
                    onClick = { onRouteChange(tab.route) },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) }
                )
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
