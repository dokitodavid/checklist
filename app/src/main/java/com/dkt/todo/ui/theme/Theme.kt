package com.dkt.todo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AshenColors {
    val Night = Color(0xFF212121)
    val Stone = Color(0xFF212121)
    val Raised = Color(0xFF303030)
    val Border = Color(0xFF454545)
    val Ember = Color(0xFFF2A541) // Âmbar do quadro de atividade
    val Gold = Color(0xFFECECEC)
    val Parchment = Color(0xFFECECEC)
    val Muted = Color(0xFFA0A0A0)
}
private val AshenScheme = darkColorScheme(
    primary = AshenColors.Parchment, onPrimary = AshenColors.Night,
    primaryContainer = AshenColors.Raised, onPrimaryContainer = AshenColors.Parchment,
    secondary = AshenColors.Parchment, onSecondary = AshenColors.Night,
    secondaryContainer = AshenColors.Raised, onSecondaryContainer = AshenColors.Parchment,
    tertiary = AshenColors.Muted, onTertiary = AshenColors.Night,
    background = AshenColors.Night, onBackground = AshenColors.Parchment,
    surface = AshenColors.Stone, onSurface = AshenColors.Parchment,
    surfaceVariant = AshenColors.Raised, onSurfaceVariant = AshenColors.Muted,
    surfaceContainerLowest = AshenColors.Night, surfaceContainerLow = AshenColors.Stone,
    surfaceContainer = AshenColors.Raised, surfaceContainerHigh = AshenColors.Raised,
    surfaceContainerHighest = AshenColors.Raised, surfaceTint = Color.Transparent,
    outline = AshenColors.Muted, outlineVariant = AshenColors.Border,
    inverseSurface = AshenColors.Parchment, inverseOnSurface = AshenColors.Night,
    inversePrimary = AshenColors.Night, error = Color(0xFFF2A694),
)
private val BaseTypography = Typography()
private val AshenTypography = Typography(
    titleLarge = BaseTypography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = BaseTypography.titleMedium.copy(fontSize = 19.sp, fontWeight = FontWeight.Medium),
)

/** Identidade fixa: as cores do wallpaper não alteram a paleta neutra. */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AshenScheme,
        typography = AshenTypography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(28.dp),
        ),
        content = content,
    )
}
