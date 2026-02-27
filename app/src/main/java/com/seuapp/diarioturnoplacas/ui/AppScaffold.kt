package com.seuapp.diarioturnoplacas.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Badge
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private val HeaderBlue = Color(0xFF356A9A)
private val ShellGray = Color(0xFFE6E7EA)
private val MenuCard = Color(0xFFF3F3F4)
private val MenuCardSelected = Color(0xFFDDEAF7)
private val MenuIcon = Color(0xFF2F6695)
private val MenuText = Color(0xFF7A7F86)

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
        containerColor = ShellGray
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ShellGray)
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AppHeader()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = ShellGray,
                            shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
                        )
                        .padding(top = 10.dp)
                ) {
                    DashboardGrid(
                        currentRoute = currentRoute,
                        onRouteChange = { currentRoute = it }
                    )
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppContent(route = currentRoute, uiState = uiState, vm = vm)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBlue)
            .padding(horizontal = 14.dp)
            .padding(top = 8.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("13:55", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("5G", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Diario", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("Turno", color = Color.White, fontStyle = FontStyle.Italic, fontSize = 20.sp)
            }

            Box {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.NotificationsNone, contentDescription = "Notificacoes", tint = Color.White)
                }
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(8.dp),
                    containerColor = Color(0xFFFF9F0A)
                ) {}
            }
        }
    }
}

@Composable
private fun DashboardGrid(currentRoute: String, onRouteChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        appTabs.chunked(3).forEach { rowTabs ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTabs.forEach { tab ->
                    val selected = tab.route == currentRoute
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(112.dp)
                            .clickable { onRouteChange(tab.route) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) MenuCardSelected else MenuCard
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = MenuIcon,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = tab.label.uppercase(),
                                color = MenuText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                repeat(3 - rowTabs.size) {
                    Spacer(modifier = Modifier.weight(1f).width(0.dp))
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
