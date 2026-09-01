package com.notzyvex.fastprint.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.notzyvex.fastprint.state.UserProfile
import com.notzyvex.fastprint.ui.components.VSpace
import com.notzyvex.fastprint.ui.icons.Lucide
import com.notzyvex.fastprint.ui.theme.LocalAppAccent
import com.notzyvex.fastprint.ui.theme.Organic
import com.notzyvex.fastprint.ui.theme.body
import com.notzyvex.fastprint.ui.theme.heading

/** fp-pop — spring scale-in, .6 -> 1.06 -> 1. */
@Composable
fun rememberPopIn(delayMillis: Int = 0): Float {
    val scale = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) kotlinx.coroutines.delay(delayMillis.toLong())
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }
    return scale.value
}

@Composable
fun LaunchScreen(onSkip: () -> Unit) {
    val accent = LocalAppAccent.current
    val scale = rememberPopIn()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Organic.Bg)
            .clickable(indication = null, interactionSource = remember {
                androidx.compose.foundation.interaction.MutableInteractionSource()
            }) { onSkip() },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(112.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(accent.value),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.Printer, null, Modifier.size(58.dp), tint = Organic.Bg)
        }
    }
}

@Composable
fun WelcomeScreen(profile: UserProfile) {
    val accent = LocalAppAccent.current
    val scale = rememberPopIn()
    val spin by rememberInfiniteTransition(label = "ring").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "ring-rotation",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(listOf(accent.light, Organic.Bg))
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.scale(scale)) {
                // The spinning progress ring around the avatar.
                androidx.compose.foundation.Canvas(Modifier.size(128.dp).rotate(spin)) {
                    val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    val inset = stroke.width / 2f
                    val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
                    drawArc(
                        color = accent.value.copy(alpha = 0.22f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = stroke,
                    )
                    drawArc(
                        color = accent.value,
                        startAngle = -90f,
                        sweepAngle = 120f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = stroke,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(accent.value, Organic.Accent2))
                        )
                        .border(4.dp, Organic.Bg, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (profile.photoUrl != null) {
                        AsyncImage(
                            model = profile.photoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    } else {
                        Text(profile.initials, style = heading(42, Color.White))
                    }
                }
            }

            VSpace(24.dp)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Welcome ${profile.firstName ?: "back"}",
                    style = heading(38),
                    textAlign = TextAlign.Center,
                )
                Heart()
            }

            VSpace(12.dp)
            profile.email?.let {
                Text("Signed in as $it", style = body(14, color = Organic.muted(0.62f)))
            }

            VSpace(24.dp)
            LoadingDots()
        }
    }
}

/** The red heart beside the welcome greeting — an SVG shape in the design, not an emoji. */
@Composable
private fun Heart() {
    androidx.compose.foundation.Canvas(Modifier.size(34.dp).padding(start = 8.dp)) {
        val w = size.width
        val h = size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.5f, h * 0.875f)
            cubicTo(w * 0.19f, h * 0.66f, w * 0.0125f, h * 0.38f, w * 0.18f, h * 0.19f)
            cubicTo(w * 0.30f, h * 0.06f, w * 0.42f, h * 0.13f, w * 0.5f, h * 0.29f)
            cubicTo(w * 0.58f, h * 0.13f, w * 0.70f, h * 0.06f, w * 0.82f, h * 0.19f)
            cubicTo(w * 0.9875f, h * 0.38f, w * 0.81f, h * 0.66f, w * 0.5f, h * 0.875f)
            close()
        }
        drawPath(path, Color(0xFFE8202A))
    }
}

@Composable
private fun LoadingDots() {
    val accent = LocalAppAccent.current
    val transition = rememberInfiniteTransition(label = "dots")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(600, delayMillis = index * 200),
                    RepeatMode.Reverse,
                ),
                label = "dot-$index",
            )
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent.value.copy(alpha = alpha)),
            )
        }
        Text(
            "Getting your profile ready…",
            style = heading(14, Organic.muted(0.7f)),
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
