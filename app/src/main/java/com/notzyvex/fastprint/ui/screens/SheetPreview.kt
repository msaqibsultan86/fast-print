package com.notzyvex.fastprint.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.notzyvex.fastprint.print.aspectRatio
import com.notzyvex.fastprint.state.DocType
import com.notzyvex.fastprint.state.ImageTransform
import com.notzyvex.fastprint.state.Margins
import com.notzyvex.fastprint.state.PrintSettings
import com.notzyvex.fastprint.state.Scale
import com.notzyvex.fastprint.ui.theme.LocalAppAccent
import com.notzyvex.fastprint.ui.theme.Organic

/** "Actual size" shrinks artwork to 72% — same constant the page renderer uses. */
private const val ACTUAL_FACTOR = 0.72f

/** Desaturates everything drawn inside — the Black & white preview. */
fun Modifier.grayscale(enabled: Boolean): Modifier = if (!enabled) this else this.drawWithCache {
    val paint = Paint().apply {
        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    }
    onDrawWithContent {
        drawIntoCanvas { canvas ->
            canvas.saveLayer(Rect(Offset.Zero, size), paint)
            drawContent()
            canvas.restore()
        }
    }
}

/**
 * The live sheet. Its proportions are the real paper's, margins inset the content, Scale picks
 * how the artwork fills it, and B&W desaturates — the same rules PageRenderer applies on paper,
 * so what the user sees here is what comes out.
 */
@Composable
fun SheetPreview(
    settings: PrintSettings,
    transform: ImageTransform,
    bitmap: android.graphics.Bitmap?,
    adjustMode: Boolean,
    modifier: Modifier = Modifier,
    maxSize: Dp = 214.dp,
    onDrag: (dxFraction: Float, dyFraction: Float) -> Unit = { _, _ -> },
    onDoubleTap: () -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val ratio = settings.aspectRatio()
    val boxW: Dp
    val boxH: Dp
    if (ratio >= 1f) {
        boxW = maxSize
        boxH = maxSize / ratio
    } else {
        boxH = maxSize
        boxW = maxSize * ratio
    }

    val padFraction = when (settings.margins) {
        Margins.NONE -> 0.03f
        Margins.NORMAL -> 0.09f
        Margins.WIDE -> 0.15f
    }

    Box(
        modifier = modifier
            .size(width = boxW, height = boxH)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .grayscale(!settings.color),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val frameW = maxWidth
            val frameH = maxHeight

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = frameW * padFraction, vertical = frameH * padFraction),
                contentAlignment = Alignment.Center,
            ) {
                if (bitmap != null) {
                    val dragModifier = if (adjustMode) {
                        Modifier.pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                onDrag(
                                    dragAmount.x / size.width.toFloat(),
                                    dragAmount.y / size.height.toFloat(),
                                )
                            }
                        }
                    } else {
                        Modifier
                    }

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Your upload",
                        contentScale = if (settings.scale == Scale.FILL) {
                            ContentScale.Crop
                        } else {
                            ContentScale.Fit
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .then(dragModifier)
                            .pointerInput(Unit) {
                                detectTapGestures(onDoubleTap = { onDoubleTap() })
                            }
                            .graphicsLayer {
                                val extra =
                                    if (settings.scale == Scale.ACTUAL) ACTUAL_FACTOR else 1f
                                scaleX = transform.zoom * extra
                                scaleY = transform.zoom * extra
                                rotationZ = transform.rotation.toFloat()
                                translationX = transform.offsetX * frameW.toPx()
                                translationY = transform.offsetY * frameH.toPx()
                            },
                    )
                } else {
                    PlaceholderArtwork(settings.docType)
                }
            }
        }
        overlay()
    }
}

/** The skeleton artwork shown before an upload, matching the prototype per docType. */
@Composable
private fun PlaceholderArtwork(docType: DocType) {
    val accent = LocalAppAccent.current
    when (docType) {
        DocType.PHOTOS -> Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Organic.Accent300, Organic.Accent2400, Organic.Accent500)
                    )
                )
        ) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp)
                    .fillMaxSize(0.3f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.55f))
            )
        }

        DocType.CARDS -> Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Bar(0.34f, 12.dp, accent.value)
            VGap(6.dp)
            Bar(0.70f, 7.dp, Organic.Text.copy(alpha = 0.78f))
            VGap(4.dp)
            Bar(0.52f, 5.dp, Organic.Text.copy(alpha = 0.30f))
            VGap(4.dp)
            Bar(0.60f, 5.dp, Organic.Text.copy(alpha = 0.30f))
        }

        else -> Column(Modifier.fillMaxSize()) {
            Bar(0.64f, 8.dp, accent.value)
            VGap(7.dp)
            Bar(1f, 5.dp, Organic.Text.copy(alpha = 0.24f))
            VGap(5.dp)
            Bar(1f, 5.dp, Organic.Text.copy(alpha = 0.24f))
            VGap(5.dp)
            Bar(0.88f, 5.dp, Organic.Text.copy(alpha = 0.24f))
            VGap(7.dp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Organic.Accent2300.copy(alpha = 0.6f))
            )
            VGap(7.dp)
            Bar(1f, 5.dp, Organic.Text.copy(alpha = 0.24f))
            VGap(5.dp)
            Bar(0.76f, 5.dp, Organic.Text.copy(alpha = 0.24f))
        }
    }
}

@Composable
private fun Bar(widthFraction: Float, barHeight: Dp, color: Color) {
    Box(
        Modifier
            .fillMaxWidth(widthFraction)
            .height(barHeight)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}

@Composable
private fun VGap(h: Dp) = Box(Modifier.height(h))
