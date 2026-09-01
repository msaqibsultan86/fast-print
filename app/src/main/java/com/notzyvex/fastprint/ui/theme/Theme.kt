package com.notzyvex.fastprint.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Organic is a single warm light theme — it does not have a dark variant, so we pin the
 * light scheme regardless of the system setting rather than let Material invent one.
 */
private fun schemeFor(accent: AppAccent) = lightColorScheme(
    primary = accent.value,
    onPrimary = Organic.Bg,
    primaryContainer = accent.light,
    onPrimaryContainer = accent.dark,
    secondary = Organic.Accent2,
    onSecondary = Organic.Bg,
    background = Organic.Bg,
    onBackground = Organic.Text,
    surface = Organic.Surface,
    onSurface = Organic.Text,
    surfaceVariant = Organic.Surface,
    onSurfaceVariant = Organic.muted(0.7f),
    outline = Organic.Divider,
    error = Organic.Danger,
    onError = Organic.Bg,
)

@Composable
fun FastPrintTheme(
    accentTheme: AccentTheme = AccentTheme.Terracotta,
    content: @Composable () -> Unit,
) {
    val accent = accentTheme.colors()
    CompositionLocalProvider(LocalAppAccent provides accent) {
        MaterialTheme(
            colorScheme = schemeFor(accent),
            typography = OrganicTypography,
            content = content,
        )
    }
}
