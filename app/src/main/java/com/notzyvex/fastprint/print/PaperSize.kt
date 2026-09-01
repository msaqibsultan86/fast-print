package com.notzyvex.fastprint.print

import android.print.PrintAttributes
import com.notzyvex.fastprint.state.Margins
import com.notzyvex.fastprint.state.Orientation
import com.notzyvex.fastprint.state.PrintSettings
import com.notzyvex.fastprint.state.PaperUnit
import kotlin.math.roundToInt

/** 1 mil = 1/1000 inch. Android's PrintAttributes.MediaSize works in mils. */
private const val MILS_PER_INCH = 1000.0
private const val CM_PER_INCH = 2.54

fun Double.toInches(unit: PaperUnit): Double =
    if (unit == PaperUnit.CM) this / CM_PER_INCH else this

/**
 * Width/height in inches with the orientation already applied — portrait keeps the
 * short edge horizontal, landscape swaps.  Mirrors the prototype's preview geometry so
 * what the user sees on the sheet is what the printer receives.
 */
fun PrintSettings.orientedInches(): Pair<Double, Double> {
    var w = width.toInches(unit)
    var h = height.toInches(unit)
    val needsSwap = if (orientation == Orientation.LANDSCAPE) w < h else w > h
    if (needsSwap) { val t = w; w = h; h = t }
    return w to h
}

/** Aspect ratio (w/h) of the oriented sheet — drives the on-screen preview and crop frame. */
fun PrintSettings.aspectRatio(): Float {
    val (w, h) = orientedInches()
    if (h <= 0.0) return 1f
    return (w / h).toFloat()
}

fun PrintSettings.mediaSize(): PrintAttributes.MediaSize {
    val (w, h) = orientedInches()
    val wMils = (w * MILS_PER_INCH).roundToInt().coerceAtLeast(1)
    val hMils = (h * MILS_PER_INCH).roundToInt().coerceAtLeast(1)
    val label = sizeLabel()
    return PrintAttributes.MediaSize("fastprint_custom", label, wMils, hMils)
}

/** Margins in inches, matching the prototype's 0 / 8mm / 18mm. */
fun PrintSettings.marginInches(): Double = when (margins) {
    Margins.NONE -> 0.0
    Margins.NORMAL -> 8.0 / 25.4
    Margins.WIDE -> 18.0 / 25.4
}

fun PrintSettings.printAttributes(): PrintAttributes = PrintAttributes.Builder()
    .setMediaSize(mediaSize())
    .setResolution(PrintAttributes.Resolution("fastprint_dpi", "${dpi} dpi", dpi, dpi))
    .setColorMode(
        if (color) PrintAttributes.COLOR_MODE_COLOR else PrintAttributes.COLOR_MODE_MONOCHROME
    )
    .setDuplexMode(
        if (duplex) PrintAttributes.DUPLEX_MODE_LONG_EDGE else PrintAttributes.DUPLEX_MODE_NONE
    )
    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
    .build()
