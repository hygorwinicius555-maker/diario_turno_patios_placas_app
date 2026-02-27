package com.seuapp.diarioturnoplacas.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

@Immutable
data class AppSpacing(
    val xxs: Int = 4,
    val xs: Int = 8,
    val sm: Int = 12,
    val md: Int = 16,
    val lg: Int = 24,
    val xl: Int = 32
)

object AppShape {
    val small = RoundedCornerShape(12.dp)
    val medium = RoundedCornerShape(16.dp)
    val large = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(999.dp)
}

val Spacing = AppSpacing()
