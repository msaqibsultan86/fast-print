package com.notzyvex.fastprint.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.notzyvex.fastprint.R
import com.notzyvex.fastprint.state.DocType
import com.notzyvex.fastprint.ui.components.IconPill
import com.notzyvex.fastprint.ui.components.VSpace
import com.notzyvex.fastprint.ui.icons.Lucide
import com.notzyvex.fastprint.ui.theme.LocalAppAccent
import com.notzyvex.fastprint.ui.theme.Organic
import com.notzyvex.fastprint.ui.theme.body
import com.notzyvex.fastprint.ui.theme.heading

private const val KOFI_URL = "https://ko-fi.com/notzyvex"

private data class FormatCard(
    val type: DocType,
    val icon: ImageVector,
    val chipBg: Color,
    val chipTint: Color,
    val solid: Boolean = false,
)

private val FormatCards = listOf(
    FormatCard(DocType.DOCUMENTS, Lucide.FileText, Organic.Accent100, Organic.Accent700),
    FormatCard(DocType.PHOTOS, Lucide.Image, Organic.Accent2100, Organic.Accent2800),
    FormatCard(DocType.LABELS, Lucide.Label, Organic.Accent100, Organic.Accent700),
    FormatCard(DocType.CARDS, Lucide.CreditCard, Organic.Accent2100, Organic.Accent2800),
    FormatCard(DocType.POSTERS, Lucide.Poster, Organic.Accent100, Organic.Accent700),
    FormatCard(DocType.CUSTOM, Lucide.Custom, Color.Transparent, Color.Unspecified, solid = true),
)

@Composable
fun HomeScreen(
    onPickFormat: (DocType) -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
) {
    val accent = LocalAppAccent.current
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().background(Organic.Bg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 22.dp, end = 22.dp, top = 24.dp, bottom = 28.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(accent.value),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Lucide.Printer, null, Modifier.size(22.dp), tint = Organic.Bg)
                        }
                        Box(Modifier.size(width = 11.dp, height = 1.dp))
                        Text("Fast Print", style = heading(20))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        IconPill(Lucide.History, "Print history", onClick = onHistory)
                        IconPill(Lucide.Settings, "Settings", onClick = onSettings)
                    }
                }
                VSpace(18.dp)
                Text("What are we\nprinting today?", style = heading(30))
                VSpace(4.dp)
                Text("Choose a format to get started.", style = body(14, color = Organic.muted()))
                VSpace(16.dp)
            }
        }

        items(FormatCards) { card ->
            FormatTile(card = card, onClick = { onPickFormat(card.type) })
        }

        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 26.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.kofi_badge),
                    contentDescription = "Support me on Ko-fi",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .widthIn(max = 230.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            context.startActivity(
                                android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    KOFI_URL.toUri(),
                                )
                            )
                        },
                )
            }
        }
    }
}

@Composable
private fun FormatTile(card: FormatCard, onClick: () -> Unit) {
    val accent = LocalAppAccent.current
    val solid = card.solid
    val titleColor = if (solid) Organic.Bg else Organic.Text
    val subColor = if (solid) Organic.Bg.copy(alpha = 0.85f) else Organic.muted()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (solid) accent.value else Organic.Surface)
            .clickable(onClick = onClick)
            .padding(18.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(if (solid) Organic.Bg.copy(alpha = 0.22f) else card.chipBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                card.icon,
                null,
                Modifier.size(24.dp),
                tint = if (solid) Organic.Bg else card.chipTint,
            )
        }
        VSpace(14.dp)
        Text(card.type.label, style = heading(16, titleColor))
        Text(card.type.subtitle, style = body(12, color = subColor))
    }
}
