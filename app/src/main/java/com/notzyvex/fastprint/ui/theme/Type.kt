package com.notzyvex.fastprint.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.notzyvex.fastprint.R

/** Display headings — Caprasimo 400. */
val Caprasimo = FontFamily(Font(R.font.caprasimo, FontWeight.Normal))

/**
 * Body — Figtree, shipped as a variable font. minSdk 26 so the weight axis is honoured
 * natively rather than synthesised.
 *
 * FontVariation is still @ExperimentalTextApi in Compose 1.7, hence the opt-in.
 */
@OptIn(ExperimentalTextApi::class)
val Figtree = FontFamily(
    Font(
        R.font.figtree,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.figtree,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.figtree,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

/** Heading helper — every Caprasimo run in the app goes through this. */
fun heading(size: Int, color: androidx.compose.ui.graphics.Color = Organic.Text) = TextStyle(
    fontFamily = Caprasimo,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = (size * 1.12f).sp,
    letterSpacing = (-0.015 * size).sp,
    color = color,
)

/** Body helper. */
fun body(
    size: Int = 15,
    weight: FontWeight = FontWeight.Normal,
    color: androidx.compose.ui.graphics.Color = Organic.Text,
) = TextStyle(
    fontFamily = Figtree,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size * 1.55f).sp,
    color = color,
)

val OrganicTypography = Typography(
    displayLarge = heading(42),
    headlineLarge = heading(32),
    headlineMedium = heading(25),
    headlineSmall = heading(20),
    titleLarge = heading(16),
    bodyLarge = body(15),
    bodyMedium = body(14),
    bodySmall = body(13),
    labelSmall = body(11),
)
