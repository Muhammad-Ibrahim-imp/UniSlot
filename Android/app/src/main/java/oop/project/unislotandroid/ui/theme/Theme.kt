package oop.project.unislotandroid.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Primary     = Color(0xFF1A56DB)
private val OnPrimary   = Color(0xFFFFFFFF)
private val Secondary   = Color(0xFF0E7490)
private val Background  = Color(0xFFF3F4F6)
private val Surface     = Color(0xFFFFFFFF)
private val Error       = Color(0xFFE02424)
private val OnBackground = Color(0xFF111827)
private val OnSurface   = Color(0xFF111827)

private val AppColorScheme = lightColorScheme(
    primary          = Primary,
    onPrimary        = OnPrimary,
    secondary        = Secondary,
    background       = Background,
    surface          = Surface,
    error            = Error,
    onBackground     = OnBackground,
    onSurface        = OnSurface,
    surfaceVariant   = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFF6B7280),
)

@Composable
fun UniSlotAndroidTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content     = content
    )
}

// Convenience colours
val GreenBadge  = Color(0xFF057A55)
val RedBadge    = Color(0xFFE02424)
val YellowBadge = Color(0xFFC27803)
val BlueBadge   = Color(0xFF1A56DB)
