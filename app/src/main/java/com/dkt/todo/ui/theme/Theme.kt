package com.dkt.todo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dkt.todo.data.AppAppearance

/** Paleta compartilhada pelo app e pelo widget. */
data class ChecklistPalette(
    val Night: Color, val Raised: Color, val Border: Color,
    val Ember: Color, val Parchment: Color, val Muted: Color,
)

private val MagePalette = ChecklistPalette(
    Night = Color(0xFF212121), Raised = Color(0xFF303030), Border = Color(0xFF454545),
    Ember = Color(0xFFF2A541), Parchment = Color(0xFFECECEC), Muted = Color(0xFFA0A0A0),
)
private val KittenPalette = ChecklistPalette(
    Night = Color(0xFFFFF8F4), Raised = Color(0xFFF6E7ED), Border = Color(0xFFE6C9D4),
    Ember = Color(0xFFB4557B), Parchment = Color(0xFF503846), Muted = Color(0xFF7E606F),
)

fun paletteFor(appearance: AppAppearance): ChecklistPalette =
    if (appearance == AppAppearance.KITTEN) KittenPalette else MagePalette

val LocalAppearance = staticCompositionLocalOf { AppAppearance.MAGE }
val LocalPalette = staticCompositionLocalOf { MagePalette }

private fun schemeFor(appearance: AppAppearance) = paletteFor(appearance).let { p ->
    val base = if (appearance == AppAppearance.KITTEN) lightColorScheme() else darkColorScheme()
    val accent = if (appearance == AppAppearance.KITTEN) Color(0xFF9C4566) else p.Parchment
    base.copy(
        primary = accent, onPrimary = if (appearance == AppAppearance.KITTEN) Color.White else p.Night,
        primaryContainer = p.Raised, onPrimaryContainer = p.Parchment,
        secondary = accent, onSecondary = if (appearance == AppAppearance.KITTEN) Color.White else p.Night,
        secondaryContainer = p.Raised, onSecondaryContainer = p.Parchment,
        tertiary = p.Muted, onTertiary = p.Night,
        background = p.Night, onBackground = p.Parchment,
        surface = p.Night, onSurface = p.Parchment,
        surfaceVariant = p.Raised, onSurfaceVariant = p.Muted,
        surfaceContainerLowest = p.Night, surfaceContainerLow = p.Night,
        surfaceContainer = p.Raised, surfaceContainerHigh = p.Raised,
        surfaceContainerHighest = p.Raised, surfaceTint = Color.Transparent,
        outline = p.Muted, outlineVariant = p.Border,
        inverseSurface = p.Parchment, inverseOnSurface = p.Night,
        inversePrimary = p.Night,
        error = if (appearance == AppAppearance.KITTEN) Color(0xFFB3263E) else Color(0xFFF2A694),
    )
}

private val BaseTypography = Typography()
private val ChecklistTypography = Typography(
    titleLarge = BaseTypography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = BaseTypography.titleMedium.copy(fontSize = 19.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun AppTheme(appearance: AppAppearance, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppearance provides appearance, LocalPalette provides paletteFor(appearance)) {
        MaterialTheme(
            colorScheme = schemeFor(appearance),
            typography = ChecklistTypography,
            shapes = Shapes(
                extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp),
                extraLarge = RoundedCornerShape(28.dp),
            ),
            content = content,
        )
    }
}
