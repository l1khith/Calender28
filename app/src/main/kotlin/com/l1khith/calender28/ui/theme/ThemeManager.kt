package com.l1khith.calender28.ui.theme

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme(val themeName: String, val isProOnly: Boolean) {
    DEFAULT("Default Dark", false),
    TERMINAL("Matrix Terminal", false),
    CYBER_SOL("Cyber Sol", true),
    NORD_FROST("Nord Frost", true),
    MONOLITH("Monolith Monochrome", true)
}

data class ThemeColors(
    val surface: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerHigh: Color,
    val primary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val textHeader: Color,
    val textSecondary: Color,
    val outlineVariant: Color
)

object ThemeManager {
    private val _currentTheme = MutableStateFlow(AppTheme.DEFAULT)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
    }

    fun getColors(theme: AppTheme): ThemeColors {
        return when (theme) {
            AppTheme.DEFAULT -> ThemeColors(
                surface = Color(0xFF131316),
                surfaceContainerLow = Color(0xFF1B1B1E),
                surfaceContainerHigh = Color(0xFF2A2A2D),
                primary = Color(0xFFADC6FF),
                primaryContainer = Color(0xFF4D8EFF),
                onPrimaryContainer = Color(0xFF00285D),
                secondary = Color(0xFFFFB95F),
                textHeader = Color(0xFFFAFAFA),
                textSecondary = Color(0xFFA1A1AA),
                outlineVariant = Color(0xFF424754)
            )
            AppTheme.CYBER_SOL -> ThemeColors(
                surface = Color(0xFF0F172A),
                surfaceContainerLow = Color(0xFF1E293B),
                surfaceContainerHigh = Color(0xFF334155),
                primary = Color(0xFFF59E0B),
                primaryContainer = Color(0xFFF59E0B),
                onPrimaryContainer = Color(0xFF78350F),
                secondary = Color(0xFFF97316),
                textHeader = Color(0xFFF8FAFC),
                textSecondary = Color(0xFF94A3B8),
                outlineVariant = Color(0xFF334155)
            )
            AppTheme.TERMINAL -> ThemeColors(
                surface = Color(0xFF09090B),
                surfaceContainerLow = Color(0xFF18181B),
                surfaceContainerHigh = Color(0xFF27272A),
                primary = Color(0xFF10B981),
                primaryContainer = Color(0xFF10B981),
                onPrimaryContainer = Color(0xFF064E3B),
                secondary = Color(0xFF34D399),
                textHeader = Color(0xFFFFFFFF),
                textSecondary = Color(0xFFA1A1AA),
                outlineVariant = Color(0xFF27272A)
            )
            AppTheme.NORD_FROST -> ThemeColors(
                surface = Color(0xFF0B132B),
                surfaceContainerLow = Color(0xFF1C2541),
                surfaceContainerHigh = Color(0xFF3A506B),
                primary = Color(0xFF38BDF8),
                primaryContainer = Color(0xFF38BDF8),
                onPrimaryContainer = Color(0xFF075985),
                secondary = Color(0xFF7DD3FC),
                textHeader = Color(0xFFF0F9FF),
                textSecondary = Color(0xFF94A3B8),
                outlineVariant = Color(0xFF3A506B)
            )
            AppTheme.MONOLITH -> ThemeColors(
                surface = Color(0xFF000000),
                surfaceContainerLow = Color(0xFF121212),
                surfaceContainerHigh = Color(0xFF262626),
                primary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFFFFFF),
                onPrimaryContainer = Color(0xFF000000),
                secondary = Color(0xFFA1A1AA),
                textHeader = Color(0xFFFFFFFF),
                textSecondary = Color(0xFFA1A1AA),
                outlineVariant = Color(0xFF333333)
            )
        }
    }
}

