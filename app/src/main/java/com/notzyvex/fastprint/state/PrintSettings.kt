package com.notzyvex.fastprint.state

import java.util.Locale

enum class Orientation { PORTRAIT, LANDSCAPE }

/** Named PaperUnit, not Unit — Unit would shadow kotlin.Unit everywhere it is imported. */
enum class PaperUnit(val label: String) { IN("in"), CM("cm") }

enum class Margins { NONE, NORMAL, WIDE }

enum class Scale { FIT, FILL, ACTUAL }

/** The six Home cards, each carrying the preset it loads into Customize. */
enum class DocType(
    val label: String,
    val subtitle: String,
    val presetWidth: Double,
    val presetHeight: Double,
    val presetUnit: PaperUnit,
    val presetOrientation: Orientation,
) {
    DOCUMENTS("Documents", "PDF & Word", 8.5, 11.0, PaperUnit.IN, Orientation.PORTRAIT),
    PHOTOS("Photos", "Glossy prints", 4.0, 6.0, PaperUnit.IN, Orientation.PORTRAIT),
    LABELS("Labels", "& stickers", 4.0, 2.0, PaperUnit.IN, Orientation.LANDSCAPE),
    CARDS("Cards", "Business", 3.5, 2.0, PaperUnit.IN, Orientation.LANDSCAPE),
    POSTERS("Posters", "Large format", 24.0, 36.0, PaperUnit.IN, Orientation.PORTRAIT),
    CUSTOM("Custom", "Any size", 8.5, 11.0, PaperUnit.IN, Orientation.PORTRAIT);

    companion object {
        fun fromLabel(label: String): DocType =
            entries.firstOrNull { it.label == label } ?: DOCUMENTS
    }
}

/**
 * How the user has positioned their uploaded image.
 *
 * offsetX/offsetY are stored NORMALISED (fraction of the sheet's width/height) rather than
 * in preview pixels as the HTML prototype did. The prototype only ever had to redraw into a
 * fixed 214px box; here the same transform has to reproduce on a 4x6 photo and a 24x36 poster,
 * so it has to be resolution-independent.
 */
data class ImageTransform(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val zoom: Float = 1f,
    val rotation: Int = 0,
) {
    fun reset() = ImageTransform()
}

data class PrintSettings(
    val docType: DocType = DocType.DOCUMENTS,
    val width: Double = 8.5,
    val height: Double = 11.0,
    val unit: PaperUnit = PaperUnit.IN,
    val orientation: Orientation = Orientation.PORTRAIT,
    val copies: Int = 1,
    val color: Boolean = true,
    val dpi: Int = 300,
    val margins: Margins = Margins.NORMAL,
    val scale: Scale = Scale.FIT,
    val duplex: Boolean = false,
) {
    fun sizeLabel(): String = "${fmt(width)} × ${fmt(height)} ${unit.label}"

    fun qualityLabel(): String = when (dpi) {
        150 -> "Draft (150 dpi)"
        600 -> "High (600 dpi)"
        else -> "Normal (300 dpi)"
    }

    fun copiesWord(): String = if (copies == 1) "copy" else "copies"

    companion object {
        fun fmt(v: Double): String =
            if (v == v.toLong().toDouble()) v.toLong().toString()
            else String.format(Locale.US, "%.4f", v).trimEnd('0').trimEnd('.')

        fun forDocType(t: DocType) = PrintSettings(
            docType = t,
            width = t.presetWidth,
            height = t.presetHeight,
            unit = t.presetUnit,
            orientation = t.presetOrientation,
        )
    }
}

/** The paper-size preset chips on the Customize screen. */
data class PaperPreset(val label: String, val w: Double, val h: Double, val unit: PaperUnit)

val PaperPresets = listOf(
    PaperPreset("Letter", 8.5, 11.0, PaperUnit.IN),
    PaperPreset("A4", 21.0, 29.7, PaperUnit.CM),
    PaperPreset("4×6", 4.0, 6.0, PaperUnit.IN),
    PaperPreset("Square", 8.0, 8.0, PaperUnit.IN),
    PaperPreset("Poster", 24.0, 36.0, PaperUnit.IN),
)
