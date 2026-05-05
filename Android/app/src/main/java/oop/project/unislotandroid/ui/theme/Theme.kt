package oop.project.unislotandroid.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Primary     = Color(0xFF1A3262)
private val OnPrimary   = Color(0xFFF9F9FC)
private val Secondary   = Color(0xFF7D8EA6)
private val Background  = Color(0xFFF3F4F6)
private val Surface     = Color(0xFFFFFFFF)
private val Error       = Color(0xFFE02424)
private val OnBackground = Color(0xFF282A2D)
private val OnSurface   = Color(0xFF282A2D)

private val AppColorScheme = lightColorScheme(
// Main brand color of the app (used for buttons, app bar, highlights)
    primary = Primary,

// Text/icons that appear ON top of primary color
    onPrimary = OnPrimary,

// Secondary accent color (used for less prominent UI elements)
    secondary = Secondary,

// App background color (overall screen background)
    background = Background,

// Surface color (cards, sheets, dialogs background)
    surface = Surface,

// Error color (used for errors, warnings, destructive actions)
    error = Error,

// Text/icons that appear on top of background color
    onBackground = OnBackground,

// Text/icons that appear on top of surface color
    onSurface = OnSurface,

// Slightly different surface shade (used for sections, cards variation)
    surfaceVariant = Color(0xFFE5E7EB),

// Text/icons on top of surfaceVariant (muted text)
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
