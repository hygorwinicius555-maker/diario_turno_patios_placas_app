package com.seuapp.diarioturnoplacas.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun CapacityGauge(value: Double, modifier: Modifier = Modifier, label: String) {
    val normalized = value.coerceIn(0.0, 1.5)
    val targetProgress = (normalized / 1.5).toFloat()
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val tertiary = MaterialTheme.colorScheme.tertiary
    val secondary = MaterialTheme.colorScheme.secondary
    val error = MaterialTheme.colorScheme.error
    val animatedProgress = animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "gauge_progress"
    )
    val gaugeColor = when {
        normalized <= 1.0 -> tertiary
        normalized <= 1.2 -> secondary
        else -> error
    }
    val animatedColor = animateColorAsState(
        targetValue = gaugeColor,
        animationSpec = tween(durationMillis = 700),
        label = "gauge_color"
    )

    Box(modifier = modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 24f
            val diameter = min(size.width, size.height) - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = surfaceVariant,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            drawArc(
                color = animatedColor.value,
                startAngle = 150f,
                sweepAngle = 240f * animatedProgress.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Text(text = label, style = MaterialTheme.typography.titleMedium)
    }
}
