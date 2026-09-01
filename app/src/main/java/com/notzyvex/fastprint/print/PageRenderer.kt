package com.notzyvex.fastprint.print

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.notzyvex.fastprint.state.ImageTransform
import com.notzyvex.fastprint.state.PrintSettings
import com.notzyvex.fastprint.state.Scale
import kotlin.math.max
import kotlin.math.min

/**
 * Draws one printed page.
 *
 * The same rules the Customize preview shows on screen are applied here, so the sheet the
 * user is looking at is genuinely what comes out: margins inset the content box, Scale picks
 * contain/cover/72%, Black & white desaturates, and the image carries the editor's
 * zoom / rotation / normalised offset.
 */
object PageRenderer {

    /** "Actual size" shrinks the content to 72%, matching the prototype. */
    private const val ACTUAL_FACTOR = 0.72f

    fun render(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        settings: PrintSettings,
        transform: ImageTransform,
        bitmap: Bitmap?,
    ) {
        canvas.drawColor(Color.WHITE)

        // Margins are expressed in inches; the canvas is in PostScript points (72/inch).
        val marginPts = (settings.marginInches() * 72.0).toFloat()
        val content = Rect(
            marginPts.toInt(),
            marginPts.toInt(),
            (pageWidth - marginPts).toInt(),
            (pageHeight - marginPts).toInt(),
        )
        if (content.width() <= 0 || content.height() <= 0) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            if (!settings.color) {
                colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
            }
        }

        if (bitmap != null) {
            drawImage(canvas, content, settings, transform, bitmap, paint)
        } else {
            drawPlaceholder(canvas, content, settings, paint)
        }
    }

    private fun drawImage(
        canvas: Canvas,
        content: Rect,
        settings: PrintSettings,
        transform: ImageTransform,
        bitmap: Bitmap,
        paint: Paint,
    ) {
        val iw = bitmap.width.toFloat()
        val ih = bitmap.height.toFloat()
        if (iw <= 0f || ih <= 0f) return

        // A 90/270 rotation swaps which image edge has to fit which content edge.
        val quarterTurned = ((transform.rotation % 180) + 180) % 180 == 90
        val fitW = if (quarterTurned) ih else iw
        val fitH = if (quarterTurned) iw else ih

        val sx = content.width() / fitW
        val sy = content.height() / fitH
        val base = when (settings.scale) {
            Scale.FILL -> max(sx, sy)                 // cover
            Scale.FIT -> min(sx, sy)                  // contain
            Scale.ACTUAL -> min(sx, sy) * ACTUAL_FACTOR
        }
        val scale = base * transform.zoom

        canvas.save()
        canvas.clipRect(content)
        canvas.translate(
            content.exactCenterX() + transform.offsetX * content.width(),
            content.exactCenterY() + transform.offsetY * content.height(),
        )
        canvas.rotate(transform.rotation.toFloat())
        canvas.scale(scale, scale)
        canvas.drawBitmap(bitmap, -iw / 2f, -ih / 2f, paint)
        canvas.restore()
    }

    /**
     * No upload — print a clean branded sheet rather than the on-screen skeleton blocks,
     * which are a preview affordance and would be meaningless on paper.
     */
    private fun drawPlaceholder(
        canvas: Canvas,
        content: Rect,
        settings: PrintSettings,
        paint: Paint,
    ) {
        val titleSize = min(content.width(), content.height()) * 0.09f
        val title = Paint(paint).apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            textSize = titleSize
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }
        val sub = Paint(paint).apply {
            color = Color.DKGRAY
            textAlign = Paint.Align.CENTER
            textSize = titleSize * 0.42f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val cx = content.exactCenterX()
        val cy = content.exactCenterY()
        canvas.drawText("Fast Print", cx, cy, title)
        canvas.drawText(
            "${settings.docType.label} · ${settings.sizeLabel()}",
            cx,
            cy + titleSize * 0.9f,
            sub,
        )
    }
}
