package com.notzyvex.fastprint.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.notzyvex.fastprint.print.aspectRatio
import com.notzyvex.fastprint.state.ImageTransform
import com.notzyvex.fastprint.state.PrintSettings
import com.notzyvex.fastprint.ui.components.HSpace
import com.notzyvex.fastprint.ui.components.IconPill
import com.notzyvex.fastprint.ui.components.VSpace
import com.notzyvex.fastprint.ui.icons.Lucide
import com.notzyvex.fastprint.ui.theme.LocalAppAccent
import com.notzyvex.fastprint.ui.theme.Organic
import com.notzyvex.fastprint.ui.theme.heading

@Composable
fun ImageEditorScreen(
    settings: PrintSettings,
    transform: ImageTransform,
    bitmap: android.graphics.Bitmap,
    onCancel: () -> Unit,
    onReset: () -> Unit,
    onZoom: (Float) -> Unit,
    onRotate: () -> Unit,
    onApply: () -> Unit,
    onDrag: (Float, Float) -> Unit,
) {
    val accent = LocalAppAccent.current
    val ratio = settings.aspectRatio()
    val maxCrop: Dp = 250.dp
    val cropW: Dp
    val cropH: Dp
    if (ratio >= 1f) {
        cropW = maxCrop
        cropH = maxCrop / ratio
    } else {
        cropH = maxCrop
        cropW = maxCrop * ratio
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Organic.Neutral900.copy(alpha = 0.72f)),
    ) {
        // ---- header ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconPill(
                icon = Lucide.X,
                contentDescription = "Cancel",
                background = Color.White.copy(alpha = 0.18f),
                tint = Color.White,
                onClick = onCancel,
            )
            Text("Edit image", style = heading(18, Color.White))
            Text(
                "Reset",
                style = heading(14, Color.White),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onReset)
                    .padding(8.dp),
            )
        }

        // ---- crop frame ----
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = cropW, height = cropH)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(2.dp, accent.value, RoundedCornerShape(8.dp)),
            ) {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val frameW = maxWidth
                    val frameH = maxHeight
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Your upload",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    onDrag(
                                        dragAmount.x / size.width.toFloat(),
                                        dragAmount.y / size.height.toFloat(),
                                    )
                                }
                            }
                            .graphicsLayer {
                                scaleX = transform.zoom
                                scaleY = transform.zoom
                                rotationZ = transform.rotation.toFloat()
                                translationX = transform.offsetX * frameW.toPx()
                                translationY = transform.offsetY * frameH.toPx()
                            },
                    )
                }
                RuleOfThirds()
            }
        }

        // ---- bottom sheet ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Organic.Bg)
                .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 30.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Lucide.ZoomIn, null, Modifier.size(18.dp), tint = Organic.Accent700)
                HSpace(12.dp)
                Slider(
                    value = transform.zoom,
                    onValueChange = onZoom,
                    valueRange = 1f..3f,
                    colors = SliderDefaults.colors(
                        thumbColor = accent.value,
                        activeTrackColor = accent.value,
                        inactiveTrackColor = Organic.Neutral300,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }

            VSpace(18.dp)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SheetAction(
                    label = "Rotate",
                    icon = Lucide.Rotate,
                    background = Organic.Surface,
                    contentColor = Organic.Text,
                    modifier = Modifier.weight(1f),
                    onClick = onRotate,
                )
                SheetAction(
                    label = "Apply",
                    icon = Lucide.Check,
                    background = accent.value,
                    contentColor = Organic.Bg,
                    modifier = Modifier.weight(1f),
                    onClick = onApply,
                )
            }
        }
    }
}

/** The classic composition grid over the crop frame. */
@Composable
private fun RuleOfThirds() {
    Canvas(Modifier.fillMaxSize()) {
        val line = Color.White.copy(alpha = 0.35f)
        val stroke = 1.dp.toPx()
        for (i in 1..2) {
            val y = size.height * i / 3f
            drawLine(line, androidx.compose.ui.geometry.Offset(0f, y),
                androidx.compose.ui.geometry.Offset(size.width, y), stroke)
            val x = size.width * i / 3f
            drawLine(line, androidx.compose.ui.geometry.Offset(x, 0f),
                androidx.compose.ui.geometry.Offset(x, size.height), stroke)
        }
    }
}

@Composable
private fun SheetAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, Modifier.size(18.dp), tint = contentColor)
        HSpace(8.dp)
        Text(label, style = heading(15, contentColor))
    }
}
