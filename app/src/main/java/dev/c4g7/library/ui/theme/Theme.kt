package dev.c4g7.library.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary = AccentRed,
    onPrimary = White,
    primaryContainer = AccentRedDim,
    onPrimaryContainer = White,
    background = Black,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkOutline,
    error = Color(0xFFCF6679),
    secondary = TextSecondary,
    onSecondary = Black
)

@Composable
fun LibraryTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current

    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context).copy(
            background = Black,
            surface = DarkSurface
        )
    } else {
        DarkColors
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
