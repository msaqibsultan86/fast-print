package com.notzyvex.fastprint.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notzyvex.fastprint.state.PrintSettings
import com.notzyvex.fastprint.ui.components.AccentButton
import com.notzyvex.fastprint.ui.components.SecondaryButton
import com.notzyvex.fastprint.ui.components.VSpace
import com.notzyvex.fastprint.ui.icons.Lucide
import com.notzyvex.fastprint.ui.theme.LocalAppAccent
import com.notzyvex.fastprint.ui.theme.Organic
import com.notzyvex.fastprint.ui.theme.body
import com.notzyvex.fastprint.ui.theme.heading

@Composable
fun PrintingScreen(settings: PrintSettings, printer: String) {
    val accent = LocalAppAccent.current
    val progress by rememberInfiniteTransition(label = "progress").animateFloat(
        initialValue = 0.04f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "progress-fill",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(accent.light, Organic.Bg)))
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PrinterIllustration()
        VSpace(30.dp)
        Text("Printing…", style = heading(24))
        VSpace(4.dp)
        Text(
            "${settings.copies} ${settings.copiesWord()} · ${settings.sizeLabel()} · to $printer",
            style = body(14, color = Organic.muted()),
            textAlign = TextAlign.Center,
        )
        VSpace(30.dp)
        Box(
            Modifier
                .fillMaxWidth(0.78f)
                .height(9.dp)
                .clip(CircleShape)
                .background(Organic.Surface),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(accent.value)
            )
        }
    }
}

@Composable
private fun PrinterIllustration() {
    val accent = LocalAppAccent.current
    Box(Modifier.size(width = 150.dp, height = 130.dp)) {
        Box(
            Modifier
                .padding(horizontal = 15.dp)
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(16.dp, 16.dp, 8.dp, 8.dp))
                .background(Organic.Neutral800)
        )
        Box(
            Modifier
                .padding(start = 32.dp, end = 32.dp, top = 14.dp)
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(accent.value)
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(66.dp)
                .clip(RoundedCornerShape(8.dp, 8.dp, 18.dp, 18.dp))
                .background(Organic.Neutral900)
        )
    }
}

@Composable
fun DoneScreen(
    settings: PrintSettings,
    printer: String,
    onPrintAgain: () -> Unit,
    onHome: () -> Unit,
) {
    val accent = LocalAppAccent.current
    val scale = rememberPopIn()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Organic.Accent2100, Organic.Bg)))
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(104.dp)
                .clip(CircleShape)
                .background(Organic.Accent2),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.Check, null, Modifier.size(52.dp), tint = Organic.Bg)
        }

        VSpace(24.dp)
        Text(
            "Print was sent to the printer",
            style = heading(28),
            textAlign = TextAlign.Center,
        )
        VSpace(6.dp)
        Text(
            "${settings.copies} ${settings.copiesWord()} on its way to $printer.",
            style = body(14, color = Organic.muted()),
            textAlign = TextAlign.Center,
        )

        VSpace(24.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Organic.Surface)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            SummaryRow("Format", settings.docType.label)
            SummaryRow("Size", settings.sizeLabel())
            SummaryRow("Quality", settings.qualityLabel())
        }

        VSpace(24.dp)
        Column(Modifier.widthIn(max = 280.dp).fillMaxWidth()) {
            SecondaryButton("Print again", onClick = onPrintAgain)
            VSpace(10.dp)
            AccentButton("Back to home", minHeight = 48.dp, onClick = onHome)
        }
    }
}

@Composable
fun FailedScreen(
    printer: String,
    reason: String?,
    onRetry: () -> Unit,
    onChoosePrinter: () -> Unit,
) {
    val scale = rememberPopIn()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Organic.DangerTint, Organic.Bg)))
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(104.dp)
                .clip(CircleShape)
                .background(Organic.Danger),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.X, null, Modifier.size(50.dp), tint = Organic.Bg)
        }

        VSpace(24.dp)
        Text("Print failed", style = heading(28), textAlign = TextAlign.Center)
        VSpace(6.dp)
        Text(
            reason?.takeIf { it.isNotBlank() }
                ?: "$printer couldn't finish the job. Check paper, ink and connection, then try again.",
            style = body(14, color = Organic.muted()),
            textAlign = TextAlign.Center,
        )

        VSpace(24.dp)
        Column(Modifier.widthIn(max = 280.dp).fillMaxWidth()) {
            AccentButton(
                text = "Try again",
                minHeight = 50.dp,
                fontSize = 15,
                leading = Lucide.Rotate,
                onClick = onRetry,
            )
            VSpace(10.dp)
            SecondaryButton("Choose another printer", onClick = onChoosePrinter)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = body(14, color = Organic.muted()))
        Text(value, style = heading(14))
    }
}
