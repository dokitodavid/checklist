package com.dkt.todo.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
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
    val Night = Color(0xFF101211)
    val Stone = Color(0xFF1A1E1B)
    val Raised = Color(0xFF242A25)
    val Border = Color(0xFF454B40)
    val Ember = Color(0xFFE7AD62)
    val Gold = Color(0xFFF3D49A)
    val Parchment = Color(0xFFE7E5D8)
    val Muted = Color(0xFFA8B09F)
    val Moss = Color(0xFFA6BD8D)
}

private val AshenScheme = darkColorScheme(
    primary = AshenColors.Ember,
    onPrimary = AshenColors.Night,
    primaryContainer = Color(0xFF493622),
    onPrimaryContainer = AshenColors.Gold,
    secondary = AshenColors.Moss,
    onSecondary = AshenColors.Night,
    secondaryContainer = Color(0xFF303C2D),
    onSecondaryContainer = Color(0xFFD8E5C9),
    background = AshenColors.Night,
    onBackground = AshenColors.Parchment,
    surface = AshenColors.Stone,
    onSurface = AshenColors.Parchment,
    surfaceVariant = AshenColors.Raised,
    onSurfaceVariant = AshenColors.Muted,
    surfaceContainer = AshenColors.Stone,
    surfaceContainerHigh = AshenColors.Raised,
    surfaceContainerHighest = AshenColors.Raised,
    outline = AshenColors.Muted,
    outlineVariant = AshenColors.Border,
    error = Color(0xFFF2A694),
)

private val BaseTypography = Typography()
private val AshenTypography = Typography(
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = FontFamily.Monospace),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = FontFamily.Monospace),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = FontFamily.Monospace, letterSpacing = 1.sp),
)

/** Identidade fixa: as cores do wallpaper não alteram a paleta da fogueira. */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AshenScheme,
        typography = AshenTypography,
        shapes = Shapes(
            extraSmall = CutCornerShape(2.dp), small = CutCornerShape(4.dp),
            medium = CutCornerShape(6.dp), large = CutCornerShape(8.dp),
            extraLarge = CutCornerShape(10.dp),
        ),
        content = content,
    )
}
