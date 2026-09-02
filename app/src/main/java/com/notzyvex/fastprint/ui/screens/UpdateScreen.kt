package com.notzyvex.fastprint.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notzyvex.fastprint.state.UpdateStage
import com.notzyvex.fastprint.ui.components.AccentButton
import com.notzyvex.fastprint.ui.components.HSpace
import com.notzyvex.fastprint.ui.components.SecondaryButton
import com.notzyvex.fastprint.ui.components.Tag
import com.notzyvex.fastprint.ui.components.VSpace
import com.notzyvex.fastprint.ui.icons.Lucide
import com.notzyvex.fastprint.ui.theme.LocalAppAccent
import com.notzyvex.fastprint.ui.theme.Organic
import com.notzyvex.fastprint.ui.theme.body
import com.notzyvex.fastprint.ui.theme.heading
import com.notzyvex.fastprint.update.AvailableUpdate

@Composable
fun UpdateScreen(
    stage: UpdateStage,
    update: AvailableUpdate,
    currentVersion: String,
    downloadPercent: Int,
    errorMessage: String?,
    onUpdateNow: () -> Unit,
    onAllowInstalls: () -> Unit,
    onNotNow: () -> Unit,
    onCancelDownload: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val accent = LocalAppAccent.current

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(accent.light, Organic.Bg))),
    ) {
        when (stage) {
            UpdateStage.AVAILABLE -> AvailableState(
                update = update,
                currentVersion = currentVersion,
                onUpdateNow = onUpdateNow,
                onLater = onDismiss,
            )

            UpdateStage.PERMISSION -> PermissionState(
                onAllow = onAllowInstalls,
                onNotNow = onNotNow,
            )

            UpdateStage.DOWNLOADING -> DownloadingState(
                update = update,
                percent = downloadPercent,
                onCancel = onCancelDownload,
            )

            UpdateStage.INSTALLING -> InstallingState(
                onCancel = onDismiss,
                onNoDialog = onRetry,
            )

            UpdateStage.FAILED -> FailedState(
                errorMessage = errorMessage,
                onRetry = onRetry,
                onLater = onDismiss,
            )
        }
    }
}

/* ---------------- available ---------------- */

@Composable
private fun AvailableState(
    update: AvailableUpdate,
    currentVersion: String,
    onUpdateNow: () -> Unit,
    onLater: () -> Unit,
) {
    val accent = LocalAppAccent.current
    val pop = rememberPopIn()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .scale(pop)
                .size(92.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(accent.value),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.Download, null, Modifier.size(46.dp), tint = Organic.Bg)
        }

        VSpace(20.dp)
        Text("Update available", style = heading(30), textAlign = TextAlign.Center)
        VSpace(4.dp)
        Text(
            "A new version of Fast Print is ready to install.",
            style = body(14, color = Organic.muted()),
            textAlign = TextAlign.Center,
        )

        VSpace(20.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tag("v$currentVersion", Organic.Neutral100, Organic.Neutral800)
            HSpace(10.dp)
            Icon(
                Lucide.ArrowRight,
                null,
                Modifier.size(20.dp),
                tint = Organic.muted(0.45f),
            )
            HSpace(10.dp)
            Tag("v${update.versionName}", Organic.Accent100, Organic.Accent800)
        }

        VSpace(8.dp)
        Text(
            "Download size · ${update.readableSize}",
            style = body(12, color = Organic.muted()),
        )

        VSpace(16.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 224.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Organic.Surface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Text("What's new", style = heading(15))
            VSpace(8.dp)
            Text(
                update.notes.ifBlank { "No release notes for this version." },
                style = body(13, color = Organic.muted(0.82f)),
            )
        }

        VSpace(20.dp)
        AccentButton(text = "Update now", onClick = onUpdateNow)
        VSpace(10.dp)
        SecondaryButton("Later", onClick = onLater)
    }
}

/* ---------------- permission ---------------- */

@Composable
private fun PermissionState(onAllow: () -> Unit, onNotNow: () -> Unit) {
    val pop = rememberPopIn()

    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .scale(pop)
                .size(92.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Organic.Accent2),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.ShieldCheck, null, Modifier.size(46.dp), tint = Color.White)
        }

        VSpace(20.dp)
        Text("Permission needed", style = heading(26), textAlign = TextAlign.Center)
        VSpace(8.dp)
        Text(
            "Android needs your OK to let Fast Print install app updates. We'll open the " +
                "system settings — turn on Allow from this source, then come back.",
            style = body(14, color = Organic.muted()),
            textAlign = TextAlign.Center,
        )

        VSpace(20.dp)
        Column(Modifier.widthIn(max = 290.dp).fillMaxWidth()) {
            AccentButton(
                text = "Allow installs",
                leading = Lucide.ChevronRight,
                onClick = onAllow,
            )
            VSpace(10.dp)
            SecondaryButton("Not now", minHeight = 48.dp, onClick = onNotNow)
        }
    }
}

/* ---------------- downloading ---------------- */

@Composable
private fun DownloadingState(
    update: AvailableUpdate,
    percent: Int,
    onCancel: () -> Unit,
) {
    val accent = LocalAppAccent.current
    val spin by rememberInfiniteTransition(label = "dl").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
        label = "dl-spin",
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(112.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(112.dp).rotate(spin)) {
                val stroke = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                val inset = stroke.width / 2f
                val arc = Size(size.width - stroke.width, size.height - stroke.width)
                drawArc(
                    color = accent.value.copy(alpha = 0.20f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arc,
                    style = stroke,
                )
                drawArc(
                    color = accent.value,
                    startAngle = -90f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arc,
                    style = stroke,
                )
            }
            Icon(Lucide.Download, null, Modifier.size(40.dp), tint = accent.value)
        }

        VSpace(26.dp)
        Text("Downloading update", style = heading(24), textAlign = TextAlign.Center)
        VSpace(4.dp)
        Text(
            "v${update.versionName} · ${update.readableSize}",
            style = body(14, color = Organic.muted()),
        )

        VSpace(26.dp)
        Column(Modifier.fillMaxWidth(0.8f)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(CircleShape)
                    .background(Organic.Surface),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth((percent / 100f).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(accent.value)
                )
            }
            VSpace(8.dp)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Downloading…", style = body(13, color = Organic.muted()))
                Text("$percent%", style = heading(13))
            }
        }

        VSpace(26.dp)
        Text(
            "Cancel",
            style = heading(14, accent.value),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onCancel)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

/* ---------------- installing ---------------- */

@Composable
private fun InstallingState(onCancel: () -> Unit, onNoDialog: () -> Unit) {
    val accent = LocalAppAccent.current
    val pop = rememberPopIn()

    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .scale(pop)
                .size(92.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(accent.value),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.ExternalLink, null, Modifier.size(46.dp), tint = Organic.Bg)
        }

        VSpace(24.dp)
        Text("Ready to install", style = heading(24), textAlign = TextAlign.Center)
        VSpace(6.dp)
        Text(
            "Android will ask you to confirm. Tap Install when its dialog appears.",
            style = body(14, color = Organic.muted()),
            textAlign = TextAlign.Center,
        )

        VSpace(24.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            repeat(3) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent.value)
                )
            }
        }

        VSpace(24.dp)
        Column(Modifier.widthIn(max = 290.dp).fillMaxWidth()) {
            SecondaryButton("Cancel", minHeight = 46.dp, onClick = onCancel)
        }

        VSpace(12.dp)
        Text(
            "Dialog didn't appear?",
            style = body(12, color = Organic.muted()),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onNoDialog)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

/* ---------------- failed ---------------- */

@Composable
private fun FailedState(
    errorMessage: String?,
    onRetry: () -> Unit,
    onLater: () -> Unit,
) {
    val pop = rememberPopIn()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .scale(pop)
                .size(92.dp)
                .clip(CircleShape)
                .background(Organic.Danger),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.X, null, Modifier.size(46.dp), tint = Color.White)
        }

        VSpace(22.dp)
        Text("Update failed", style = heading(26), textAlign = TextAlign.Center)
        VSpace(8.dp)
        Text(
            "We couldn't finish the update. Check your connection and that installs are " +
                "allowed, then try again.",
            style = body(14, color = Organic.muted()),
            textAlign = TextAlign.Center,
        )

        if (!errorMessage.isNullOrBlank()) {
            VSpace(22.dp)
            Row(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Organic.Surface)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Icon(
                    Lucide.AlertCircle,
                    null,
                    Modifier.size(18.dp),
                    tint = Organic.Danger,
                )
                HSpace(10.dp)
                Text(errorMessage, style = body(12, color = Organic.muted(0.75f)))
            }
        }

        VSpace(22.dp)
        Column(Modifier.widthIn(max = 290.dp).fillMaxWidth()) {
            AccentButton(
                text = "Try again",
                minHeight = 50.dp,
                fontSize = 15,
                leading = Lucide.RefreshCw,
                onClick = onRetry,
            )
            VSpace(10.dp)
            SecondaryButton("Later", minHeight = 46.dp, onClick = onLater)
        }
    }
}
