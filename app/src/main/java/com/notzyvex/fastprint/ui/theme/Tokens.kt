package com.notzyvex.fastprint.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * The "Organic" design system, transcribed from the handoff styles.css.
 * These are the single source of truth for colour — do not introduce values outside this file.
 */
object Organic {
    val Bg = Color(0xFFF5EAD8)
    val Surface = Color(0xFFEBDDC5)
    val Text = Color(0xFF201E1D)

    val Accent = Color(0xFFC67139)      // terracotta
    val Accent2 = Color(0xFF7A8A5E)     // sage

    // neutral ramp
    val Neutral100 = Color(0xFFF9F4ED)
    val Neutral200 = Color(0xFFEEE7DB)
    val Neutral300 = Color(0xFFDCD3C4)
    val Neutral400 = Color(0xFFC0B6A5)
    val Neutral500 = Color(0xFFA19786)
    val Neutral600 = Color(0xFF82796A)
    val Neutral700 = Color(0xFF645C50)
    val Neutral800 = Color(0xFF474238)
    val Neutral900 = Color(0xFF2E2B25)

    // accent ramp
    val Accent100 = Color(0xFFFFF2EB)
    val Accent200 = Color(0xFFFFE1D0)
    val Accent300 = Color(0xFFFFC6A5)
    val Accent400 = Color(0xFFF6A06B)
    val Accent500 = Color(0xFFD67F48)
    val Accent600 = Color(0xFFB2622D)
    val Accent700 = Color(0xFF8C491A)
    val Accent800 = Color(0xFF643312)
    val Accent900 = Color(0xFF402310)

    // accent-2 ramp
    val Accent2100 = Color(0xFFF0FAE1)
    val Accent2200 = Color(0xFFE1EECC)
    val Accent2300 = Color(0xFFCCDBB2)
    val Accent2400 = Color(0xFFAEBF92)
    val Accent2500 = Color(0xFF8FA073)
    val Accent2600 = Color(0xFF728157)
    val Accent2700 = Color(0xFF56633F)
    val Accent2800 = Color(0xFF3D472B)
    val Accent2900 = Color(0xFF272E1B)

    val Danger = Color(0xFFD24B3E)
    val DangerText = Color(0xFFB23528)
    val DangerTint = Color(0xFFFFE3DF)

    /** --color-divider: ink at 16% */
    val Divider = Text.copy(alpha = 0.16f)

    fun muted(alpha: Float = 0.55f) = Text.copy(alpha = alpha)

    // spacing scale (px in CSS -> dp here)
    val Space1 = 4.4.dp
    val Space2 = 8.8.dp
    val Space3 = 13.2.dp
    val Space4 = 17.6.dp
    val Space6 = 26.4.dp
    val Space8 = 35.2.dp
}

/** The switchable app accent (Terracotta default / Sage). Mirrors --app-accent* in the prototype. */
enum class AccentTheme(val label: String) { Terracotta("Terracotta"), Sage("Sage") }

@Immutable
data class AppAccent(
    val value: Color,
    val dark: Color,
    val light: Color,
)

fun AccentTheme.colors(): AppAccent = when (this) {
    AccentTheme.Terracotta -> AppAccent(Organic.Accent, Organic.Accent700, Organic.Accent100)
    AccentTheme.Sage -> AppAccent(Organic.Accent2, Organic.Accent2700, Organic.Accent2100)
}

val LocalAppAccent = staticCompositionLocalOf { AccentTheme.Terracotta.colors() }
