package com.notzyvex.fastprint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notzyvex.fastprint.print.DiscoveredPrinter
import com.notzyvex.fastprint.ui.components.AccentButton
import com.notzyvex.fastprint.ui.components.HSpace
import com.notzyvex.fastprint.ui.components.IconPill
import com.notzyvex.fastprint.ui.components.VSpace
import com.notzyvex.fastprint.ui.icons.Lucide
import com.notzyvex.fastprint.ui.theme.LocalAppAccent
import com.notzyvex.fastprint.ui.theme.Organic
import com.notzyvex.fastprint.ui.theme.body
import com.notzyvex.fastprint.ui.theme.heading

@Composable
fun PrintersScreen(
    printers: List<DiscoveredPrinter>,
    selected: String?,
    onBack: () -> Unit,
    onSelect: (String) -> Unit,
    onPrint: () -> Unit,
) {
    val accent = LocalAppAccent.current
    val context = LocalContext.current

    Box(Modifier.fillMaxSize().background(Organic.Bg)) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconPill(Lucide.ChevronLeft, "Back", onClick = onBack)
                HSpace(12.dp)
                Column {
                    Text("Choose printer", style = heading(19))
                    Text(
                        if (printers.isEmpty()) "Scanning…" else "${printers.size} nearby",
                        style = body(12, color = Organic.muted()),
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp, end = 20.dp, top = 6.dp, bottom = 120.dp,
                ),
            ) {
                if (printers.isEmpty()) {
                    item { ScanningState() }
                }

                items(printers, key = { it.id }) { printer ->
                    PrinterRow(
                        printer = printer,
                        selected = printer.name == selected,
                        onClick = { onSelect(printer.name) },
                    )
                }

                item {
                    // The prototype's placeholder is a real action here: Android adds printers
                    // through print services, which live in system settings.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.5.dp, Organic.Divider, RoundedCornerShape(20.dp))
                            .clickable {
                                runCatching {
                                    context.startActivity(
                                        android.content.Intent(
                                            android.provider.Settings.ACTION_PRINT_SETTINGS
                                        )
                                    )
                                }
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Lucide.Plus,
                            null,
                            Modifier.size(18.dp),
                            tint = Organic.muted(0.6f),
                        )
                        HSpace(8.dp)
                        Text("Add a printer", style = heading(14, Organic.muted(0.6f)))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(0f to Color.Transparent, 0.32f to Organic.Bg)
                )
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 26.dp),
        ) {
            AccentButton(text = "Print now", onClick = onPrint)
        }
    }
}

@Composable
private fun ScanningState() {
    val accent = LocalAppAccent.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            color = accent.value,
            strokeWidth = 3.dp,
            modifier = Modifier.size(34.dp),
        )
        VSpace(18.dp)
        Text("Looking for printers", style = heading(18))
        VSpace(6.dp)
        Text(
            "Make sure your printer is on the same Wi-Fi network. " +
                "You can also pick any printer in the system dialog after tapping Print now.",
            style = body(13, color = Organic.muted()),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PrinterRow(
    printer: DiscoveredPrinter,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = LocalAppAccent.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 11.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Organic.Surface)
            .then(
                if (selected) {
                    Modifier.border(2.dp, accent.value, RoundedCornerShape(20.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) accent.light else Organic.Neutral200),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Lucide.Printer,
                null,
                Modifier.size(22.dp),
                tint = if (selected) accent.dark else Organic.Neutral700,
            )
        }
        HSpace(13.dp)
        Column(Modifier.weight(1f)) {
            Text(printer.name, style = heading(16), maxLines = 1)
            Text(printer.statusLine, style = body(12, color = Organic.muted()), maxLines = 1)
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) accent.value else Color.Transparent)
                .then(
                    if (selected) Modifier else Modifier.border(1.5.dp, Organic.Divider, CircleShape)
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(Lucide.Check, null, Modifier.size(14.dp), tint = Organic.Bg)
            }
        }
    }
}
