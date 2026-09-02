package com.notzyvex.fastprint.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The Lucide (https://lucide.dev, ISC) glyphs the design uses, transcribed from the handoff
 * prototype so the stroke geometry matches exactly.
 *
 * Stroke width is 2.75 per the design system. Colour is baked as black and recoloured at draw
 * time by Icon's tint, so one vector serves every accent.
 */
private const val STROKE_WIDTH = 2.75f

private fun lucide(name: String, vararg pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        pathData.forEach { d ->
            addPath(
                pathData = addPathNodes(d),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = STROKE_WIDTH,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

/** SVG <circle> has no path equivalent, so express it as two arcs. */
private fun circle(cx: Float, cy: Float, r: Float): String =
    "M${cx - r} $cy a$r $r 0 1 0 ${2 * r} 0 a$r $r 0 1 0 ${-2 * r} 0"

/** SVG <rect rx> as a rounded-rect path. */
private fun rect(x: Float, y: Float, w: Float, h: Float, rx: Float): String {
    val ix = w - 2 * rx
    val iy = h - 2 * rx
    return "M${x + rx} $y h$ix a$rx $rx 0 0 1 $rx $rx v$iy a$rx $rx 0 0 1 ${-rx} $rx " +
        "h${-ix} a$rx $rx 0 0 1 ${-rx} ${-rx} v${-iy} a$rx $rx 0 0 1 $rx ${-rx} z"
}

object Lucide {

    val Printer = lucide(
        "printer",
        "M6 9V3a1 1 0 0 1 1-1h10a1 1 0 0 1 1 1v6",
        "M6 18H5a3 3 0 0 1-3-3v-3a3 3 0 0 1 3-3h14a3 3 0 0 1 3 3v3a3 3 0 0 1-3 3h-1",
        rect(6f, 14f, 12f, 8f, 1f),
    )

    val Settings = lucide(
        "settings",
        circle(12f, 12f, 3f),
        "M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 " +
            "0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 " +
            "1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 " +
            "1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 " +
            "0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 " +
            "0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 " +
            "2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 " +
            "1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z",
    )

    val History = lucide(
        "history",
        "M3 3v5h5",
        "M3.05 13A9 9 0 1 0 6 5.3L3 8",
        "M12 7v5l4 2",
    )

    val ChevronLeft = lucide("chevron-left", "m15 18-6-6 6-6")
    val ChevronRight = lucide("chevron-right", "m9 18 6-6-6-6")
    val Plus = lucide("plus", "M12 5v14M5 12h14")
    val Minus = lucide("minus", "M5 12h14")
    val Check = lucide("check", "M20 6 9 17l-5-5")
    val X = lucide("x", "M18 6 6 18M6 6l12 12")

    val Trash = lucide(
        "trash",
        "M3 6h18M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6",
    )

    val Upload = lucide(
        "upload",
        "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4",
        "M17 8 12 3 7 8",
        "M12 3v13",
    )

    val Move = lucide(
        "move",
        "M5 9l-3 3 3 3M9 5l3-3 3 3M15 19l-3 3-3-3M19 9l3 3-3 3M2 12h20M12 2v20",
    )

    val Rotate = lucide("rotate", "M3 12a9 9 0 1 0 3-6.7L3 8", "M3 3v5h5")

    val ZoomIn = lucide(
        "zoom-in",
        circle(11f, 11f, 7f),
        "M21 21l-4.3-4.3M8 11h6M11 8v6",
    )

    val User = lucide(
        "user",
        "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2",
        circle(12f, 7f, 4f),
    )

    val Bell = lucide(
        "bell",
        "M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9",
        "M13.73 21a2 2 0 0 1-3.46 0",
    )

    val Info = lucide("info", circle(12f, 12f, 10f), "M12 16v-4M12 8h.01")

    val LogOut = lucide(
        "log-out",
        "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4",
        "M16 17l5-5-5-5M21 12H9",
    )

    val FileText = lucide(
        "file-text",
        "M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7z",
        "M14 2v5h5",
        "M8 13h8M8 17h6",
    )

    val Image = lucide(
        "image",
        rect(3f, 3f, 18f, 18f, 3f),
        circle(9f, 9f, 1.6f),
        "m21 15-4.5-4.5L6 21",
    )

    val Label = lucide(
        "label",
        "M12.6 2.6a2 2 0 0 0-1.4-.6H4a2 2 0 0 0-2 2v7.2a2 2 0 0 0 .6 1.4l8.8 8.8a2 2 0 0 0 2.8 " +
            "0l6.4-6.4a2 2 0 0 0 0-2.8z",
        circle(7.5f, 7.5f, 1.5f),
    )

    val CreditCard = lucide("credit-card", rect(2f, 5f, 20f, 14f, 3f), "M2 10h20M6 15h4")

    val Poster = lucide("poster", rect(4f, 2f, 16f, 20f, 2f), "M8 7h8M8 12h8M8 17h5")

    val Custom = lucide(
        "custom",
        "M8 3H5a2 2 0 0 0-2 2v3m18 0V5a2 2 0 0 0-2-2h-3M3 16v3a2 2 0 0 0 2 2h3m8 0h3a2 2 0 0 0 2-2v-3",
    )

    val Wifi = lucide(
        "wifi",
        "M5 12.55a11 11 0 0 1 14.08 0",
        "M1.42 9a16 16 0 0 1 21.16 0",
        "M8.53 16.11a6 6 0 0 1 6.95 0",
        "M12 20h.01",
    )

    // ---- update flow ----

    val Download = lucide("download", "M12 3v12", "m7 12 5 5 5-5", "M5 21h14")

    val ShieldCheck = lucide(
        "shield-check",
        "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z",
        "m9 12 2 2 4-4",
    )

    val ExternalLink = lucide(
        "external-link",
        "M21 2 13 10",
        "M16 2h5v5",
        "M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h7",
    )

    val RefreshCw = lucide(
        "refresh-cw",
        "M3 12a9 9 0 0 1 15-6.7L21 8",
        "M21 3v5h-5",
        "M21 12a9 9 0 0 1-15 6.7L3 16",
        "M3 21v-5h5",
    )

    val AlertCircle = lucide("alert-circle", circle(12f, 12f, 10f), "M12 8v4M12 16h.01")

    val ArrowRight = lucide("arrow-right", "M5 12h14M13 6l6 6-6 6")
}
