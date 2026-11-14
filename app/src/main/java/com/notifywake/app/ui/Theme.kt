package com.notifywake.app.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

// Lavender / violet palette in the spirit of Material 3 Expressive.
private val LightColors = lightColorScheme(
    primary = Color(0xFF7438D6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBDCFF),
    onPrimaryContainer = Color(0xFF2A0064),
    secondary = Color(0xFF8E4FD8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2DAFF),
    onSecondaryContainer = Color(0xFF33004F),
    tertiary = Color(0xFFB4306B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E6),
    onTertiaryContainer = Color(0xFF3E0021),
    background = Color(0xFFFDF7FF),
    onBackground = Color(0xFF1D1A22),
    surface = Color(0xFFFDF7FF),
    onSurface = Color(0xFF1D1A22),
    surfaceVariant = Color(0xFFE8DFF2),
    onSurfaceVariant = Color(0xFF4A4455),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F0FF),
    surfaceContainer = Color(0xFFF2E9FC),
    surfaceContainerHigh = Color(0xFFECE3F6),
    surfaceContainerHighest = Color(0xFFE6DDF0),
    outline = Color(0xFF7B7486),
    outlineVariant = Color(0xFFCCC3D7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD4BBFF),
    onPrimary = Color(0xFF41008B),
    primaryContainer = Color(0xFF5B1BBD),
    onPrimaryContainer = Color(0xFFEBDCFF),
    secondary = Color(0xFFE2B6FF),
    onSecondary = Color(0xFF4F0880),
    secondaryContainer = Color(0xFF6A2E9C),
    onSecondaryContainer = Color(0xFFF2DAFF),
    tertiary = Color(0xFFFFB0CC),
    onTertiary = Color(0xFF640038),
    tertiaryContainer = Color(0xFF8E1651),
    onTertiaryContainer = Color(0xFFFFD8E6),
    background = Color(0xFF150F1F),
    onBackground = Color(0xFFE8E0EE),
    surface = Color(0xFF150F1F),
    onSurface = Color(0xFFE8E0EE),
    surfaceVariant = Color(0xFF4A4455),
    onSurfaceVariant = Color(0xFFCCC3D7),
    surfaceContainerLowest = Color(0xFF100A19),
    surfaceContainerLow = Color(0xFF1E1728),
    surfaceContainer = Color(0xFF231B2D),
    surfaceContainerHigh = Color(0xFF2D2538),
    surfaceContainerHighest = Color(0xFF383043),
    outline = Color(0xFF958DA1),
    outlineVariant = Color(0xFF4A4455),
)

// Expressive: rounder, softer corners everywhere.
private val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

// Expressive: heavier, tighter display and headline styles for strong hierarchy.
private fun expressiveTypography(): Typography {
    val base = Typography()
    fun TextStyle.bold(spacing: Double = -0.02) =
        copy(fontWeight = FontWeight.Bold, letterSpacing = spacing.em)
    return base.copy(
        displayLarge = base.displayLarge.bold(-0.03),
        displayMedium = base.displayMedium.bold(-0.03),
        displaySmall = base.displaySmall.bold(),
        headlineLarge = base.headlineLarge.bold(),
        headlineMedium = base.headlineMedium.bold(),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun NotifyWakeTheme(wallpaperColors: Boolean, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val colors = when {
        wallpaperColors && Build.VERSION.SDK_INT >= 31 ->
            if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colors,
        motionScheme = MotionScheme.expressive(),
        shapes = ExpressiveShapes,
        typography = expressiveTypography(),
        content = content,
    )
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
