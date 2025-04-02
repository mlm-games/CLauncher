package app.clauncher.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),          // light-blue
    onPrimary = Color(0xFF000000),        // black text
    primaryContainer = Color(0xFF1E1E1E), // slightly lighter than background for containers
    onPrimaryContainer = Color(0xFFE0E0E0), // Light text on primary containers

    secondary = Color(0xFF81C784),        // light-green
    onSecondary = Color(0xFF000000),      // black text
    secondaryContainer = Color(0xFF2E2E2E),
    onSecondaryContainer = Color(0xFFE0E0E0),

    tertiary = Color(0xFFFFB74D),         // Orange
    onTertiary = Color(0xFF000000),

    background = Color(0xFF121212),       // Dark background
    onBackground = Color(0xFFE0E0E0),     // Light text

    surface = Color(0xFF1D1D1D),          // Dark surface
    onSurface = Color(0xFFE0E0E0),

    surfaceVariant = Color(0xFF2D2D2D),   // Variant surface color
    onSurfaceVariant = Color(0xFFCCCCCC),

    error = Color(0xFFCF6679),            // Error color
    onError = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),          // Blue
    onPrimary = Color(0xFFFFFFFF),        // White text
    primaryContainer = Color(0xFFE3F2FD), // Light blue
    onPrimaryContainer = Color(0xFF0D47A1), // Dark blue

    secondary = Color(0xFF388E3C),        // Green
    onSecondary = Color(0xFFFFFFFF),      // White text
    secondaryContainer = Color(0xFFE8F5E9), // Light green
    onSecondaryContainer = Color(0xFF1B5E20), // Dark green

    tertiary = Color(0xFFE65100),         // Orange
    onTertiary = Color(0xFFFFFFFF),       // White

    background = Color(0xFFF5F5F5),       // Light
    onBackground = Color(0xFF212121),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF212121),

    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF616161), // Gray

    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun CLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, //ToDO: add as an option in settingsScreen
    content: @Composable () -> Unit
) {
    val colorScheme = when {
         dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
             val context = LocalContext.current
             if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
         }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}