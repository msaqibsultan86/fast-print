package com.notzyvex.fastprint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notzyvex.fastprint.ui.components.OutlineButton
import com.notzyvex.fastprint.ui.components.VSpace
import com.notzyvex.fastprint.ui.icons.Lucide
import com.notzyvex.fastprint.ui.theme.LocalAppAccent
import com.notzyvex.fastprint.ui.theme.Organic
import com.notzyvex.fastprint.ui.theme.body
import com.notzyvex.fastprint.ui.theme.heading

/**
 * The official four-colour Google "G", as required by Google's branding rules for the
 * Sign in with Google button.
 */
private val GoogleG: ImageVector = ImageVector.Builder(
    name = "google-g",
    defaultWidth = 48.dp,
    defaultHeight = 48.dp,
    viewportWidth = 48f,
    viewportHeight = 48f,
).apply {
    fun leaf(color: Long, d: String) = addPath(
        pathData = addPathNodes(d),
        fill = SolidColor(Color(color)),
        pathFillType = PathFillType.NonZero,
    )
    leaf(
        0xFFEA4335,
        "M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 " +
            "5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z",
    )
    leaf(
        0xFF4285F4,
        "M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 " +
            "7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z",
    )
    leaf(
        0xFFFBBC05,
        "M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 " +
            "20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z",
    )
    leaf(
        0xFF34A853,
        "M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 " +
            "0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z",
    )
}.build()

@Composable
fun SignInScreen(
    errorMessage: String?,
    onGoogle: () -> Unit,
    onGuest: () -> Unit,
    onDismissError: () -> Unit,
) {
    val accent = LocalAppAccent.current

    Box(Modifier.fillMaxSize()) {
        // The handoff used a remote stock video here. Shipping a hard dependency on someone
        // else's CDN is not something to ship, and no licensed clip came with the bundle, so
        // this is the branded static hero the spec allows as the alternative.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Organic.Accent800,
                            accent.value,
                            Organic.Accent2700,
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite,
                    )
                )
        )
        // Top-to-bottom scrim so the brand block stays legible over the hero.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x47141008),
                        0.26f to Color(0x00141008),
                        0.64f to Color(0x8C141008),
                        1f to Color(0xD1141008),
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(accent.value),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Lucide.Printer, null, Modifier.size(46.dp), tint = Color.White)
            }
            VSpace(16.dp)
            Text("Fast Print", style = heading(34, Color.White))
            VSpace(8.dp)
            Text(
                "Print anything. Any size. Beautifully.",
                style = body(14, color = Color.White.copy(alpha = 0.82f)),
                textAlign = TextAlign.Center,
            )
        }

        // Frosted cream sheet
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp))
                .background(Organic.Bg.copy(alpha = 0.94f))
                .padding(start = 26.dp, end = 26.dp, top = 30.dp, bottom = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(width = 44.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(Organic.Text.copy(alpha = 0.2f))
            )
            VSpace(12.dp)
            Text("Welcome", style = heading(23))
            VSpace(4.dp)
            Text(
                "Sign in to save your printers and presets.",
                style = body(13, color = Organic.muted()),
                textAlign = TextAlign.Center,
            )
            VSpace(14.dp)

            if (errorMessage != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Organic.DangerTint)
                        .clickable { onDismissError() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(errorMessage, style = body(12, color = Organic.DangerText))
                }
                VSpace(12.dp)
            }

            // Google button — white pill, official G, per Google branding rules.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 54.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Organic.Divider, CircleShape)
                    .clickable { onGoogle() }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(GoogleG, null, Modifier.size(22.dp), tint = Color.Unspecified)
                Box(Modifier.size(width = 12.dp, height = 1.dp))
                Text("Continue with Google", style = heading(16, Organic.Text))
            }

            VSpace(12.dp)

            OutlineButton(
                text = "Continue as guest",
                leading = Lucide.User,
                onClick = onGuest,
            )

            VSpace(10.dp)
            Text(
                "By continuing you agree to our Terms & Privacy Policy.",
                style = body(11, color = Organic.muted()),
                textAlign = TextAlign.Center,
            )
        }
    }
}
