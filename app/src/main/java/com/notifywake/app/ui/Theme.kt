package com.notifywake.app.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F5BD5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDFE0FF),
    onPrimaryContainer = Color(0xFF0B1366),
    secondary = Color(0xFF5B5D72),
    secondaryContainer = Color(0xFFE0E1F9),
    background = Color(0xFFF8F8FC),
    surface = Color(0xFFF8F8FC),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBCC2FF),
    onPrimary = Color(0xFF1B2678),
    primaryContainer = Color(0xFF343F9C),
    onPrimaryContainer = Color(0xFFDFE0FF),
    secondary = Color(0xFFC4C5DD),
    secondaryContainer = Color(0xFF434659),
    background = Color(0xFF121318),
    surface = Color(0xFF121318),
)

@Composable
fun NotifyWakeTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= 31 -> if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}

/** Status colours that read well on both themes. */
object StatusColors {
    val ok: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF81C995) else Color(0xFF1E8E3E)

    val warn: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFFFDD663) else Color(0xFFB06000)
}
