package com.l1khith.calender28.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object MatrixColors {
    private val activeTheme: AppTheme get() = ThemeManager.currentTheme.value
    private val colors: ThemeColors get() = ThemeManager.getColors(activeTheme)

    val Surface: Color get() = colors.surface
    val SurfaceDim: Color get() = colors.surface
    val SurfaceBright: Color get() = colors.surfaceContainerHigh
    val SurfaceContainerLowest: Color get() = colors.surface
    val SurfaceContainerLow: Color get() = colors.surfaceContainerLow
    val SurfaceContainer: Color get() = colors.surfaceContainerLow
    val SurfaceContainerHigh: Color get() = colors.surfaceContainerHigh
    val SurfaceContainerHighest: Color get() = colors.surfaceContainerHigh

    val OnSurface: Color get() = colors.textHeader
    val OnSurfaceVariant: Color get() = colors.textSecondary
    val InverseSurface: Color get() = colors.textHeader
    val InverseOnSurface: Color get() = colors.surface

    val Outline: Color get() = colors.textSecondary
    val OutlineVariant: Color get() = colors.outlineVariant
    val SurfaceTint: Color get() = colors.primary

    val Primary: Color get() = colors.primary
    val OnPrimary: Color get() = Color.Black
    val PrimaryContainer: Color get() = colors.primaryContainer
    val OnPrimaryContainer: Color get() = colors.onPrimaryContainer
    val InversePrimary: Color get() = colors.primary

    val Secondary: Color get() = colors.secondary
    val OnSecondary: Color get() = Color.Black
    val SecondaryContainer: Color get() = colors.secondary
    val OnSecondaryContainer: Color get() = Color.Black

    val Tertiary: Color get() = Color(0xFF4EDEA3)
    val OnTertiary: Color get() = Color(0xFF003824)
    val TertiaryContainer: Color get() = Color(0xFF00A572)
    val OnTertiaryContainer: Color get() = Color(0xFF00311F)

    val Error: Color get() = Color(0xFFFFB4AB)
    val OnError: Color get() = Color(0xFF690005)
    val ErrorContainer: Color get() = Color(0xFF93000A)
    val OnErrorContainer: Color get() = Color(0xFFFFDAD6)

    val TextHeader: Color get() = colors.textHeader
    val TextSecondary: Color get() = colors.textSecondary
}

object MatrixShapes {
    val Sm = RoundedCornerShape(4.dp)
    val Md = RoundedCornerShape(8.dp)
    val Lg = RoundedCornerShape(16.dp)
    val Xl = RoundedCornerShape(24.dp)
}

@Composable
fun MatrixTheme(
    content: @Composable () -> Unit
) {
    val currentTheme by ThemeManager.currentTheme.collectAsState()
    val colors = ThemeManager.getColors(currentTheme)

    val colorScheme = darkColorScheme(
        primary = colors.primary,
        onPrimary = Color.Black,
        primaryContainer = colors.primaryContainer,
        onPrimaryContainer = colors.onPrimaryContainer,
        secondary = colors.secondary,
        background = colors.surface,
        onBackground = colors.textHeader,
        surface = colors.surfaceContainerLow,
        onSurface = colors.textHeader,
        surfaceVariant = colors.surfaceContainerHigh,
        onSurfaceVariant = colors.textSecondary,
        outlineVariant = colors.outlineVariant
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

