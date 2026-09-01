package com.notzyvex.fastprint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.notzyvex.fastprint.data.JobStatus
import com.notzyvex.fastprint.data.PrintJobEntity
import com.notzyvex.fastprint.ui.components.HSpace
import com.notzyvex.fastprint.ui.components.IconPill
import com.notzyvex.fastprint.ui.components.VSpace
import com.notzyvex.fastprint.ui.icons.Lucide
import com.notzyvex.fastprint.ui.theme.LocalAppAccent
import com.notzyvex.fastprint.ui.theme.Organic
import com.notzyvex.fastprint.ui.theme.body
import com.notzyvex.fastprint.ui.theme.heading
import java.io.File
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(
    jobs: List<PrintJobEntity>,
    onBack: () -> Unit,
    onReprint: (PrintJobEntity) -> Unit,
    onDelete: (PrintJobEntity) -> Unit,
    onClearAll: () -> Unit,
) {
    val accent = LocalAppAccent.current

    Column(Modifier.fillMaxSize().background(Organic.Bg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconPill(Lucide.ChevronLeft, "Back", onClick = onBack)
            HSpace(12.dp)
            Text("Print history", style = heading(22), modifier = Modifier.weight(1f))
            if (jobs.isNotEmpty()) {
                Text(
                    "Clear all",
                    style = heading(13, accent.value),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onClearAll)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }

        if (jobs.isEmpty()) {
            EmptyHistory()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp, end = 20.dp, bottom = 40.dp,
                ),
            ) {
                items(jobs, key = { it.id }) { job ->
                    JobRow(
                        job = job,
                        onReprint = { onReprint(job) },
                        onDelete = { onDelete(job) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistory() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Organic.Surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.History, null, Modifier.size(34.dp), tint = Organic.muted())
        }
        VSpace(16.dp)
        Text("No prints yet", style = heading(18))
        VSpace(6.dp)
        Text(
            "Your printed jobs will show up here.",
            style = body(13, color = Organic.muted()),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun JobRow(
    job: PrintJobEntity,
    onReprint: () -> Unit,
    onDelete: () -> Unit,
) {
    val accent = LocalAppAccent.current
    val done = job.jobStatus() == JobStatus.DONE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Organic.Surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ---- thumbnail with status badge ----
        Box(Modifier.size(64.dp), contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                val file = job.imagePath?.let { File(it) }
                if (file != null && file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        Lucide.FileText,
                        null,
                        Modifier.size(26.dp),
                        tint = Organic.Accent700,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (done) Organic.Accent2 else Organic.Danger),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (done) Lucide.Check else Lucide.X,
                    null,
                    Modifier.size(15.dp),
                    tint = Color.White,
                )
            }
        }

        HSpace(14.dp)

        Column(Modifier.weight(1f)) {
            Text(job.docType, style = heading(16), maxLines = 1)
            Text(
                job.metaLine(),
                style = body(12, color = Organic.muted()),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(Date(job.createdAt)),
                style = body(11, color = Organic.muted(0.45f)),
                maxLines = 1,
            )
        }

        HSpace(8.dp)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IconPill(
                icon = Lucide.Printer,
                contentDescription = "Print again",
                background = accent.value,
                tint = Organic.Bg,
                iconSize = 18.dp,
                onClick = onReprint,
            )
            IconPill(
                icon = Lucide.Trash,
                contentDescription = "Delete",
                background = Organic.Bg,
                border = Organic.Divider,
                tint = Organic.DangerText,
                iconSize = 17.dp,
                onClick = onDelete,
            )
        }
    }
}
