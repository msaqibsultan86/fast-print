package com.notzyvex.fastprint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.notzyvex.fastprint.state.ImageTransform
import com.notzyvex.fastprint.state.Margins
import com.notzyvex.fastprint.state.Orientation
import com.notzyvex.fastprint.state.PaperPreset
import com.notzyvex.fastprint.state.PaperPresets
import com.notzyvex.fastprint.state.PaperUnit
import com.notzyvex.fastprint.state.PrintSettings
import com.notzyvex.fastprint.state.Scale
import com.notzyvex.fastprint.ui.components.AccentButton
import com.notzyvex.fastprint.ui.components.Chip
import com.notzyvex.fastprint.ui.components.HSpace
import com.notzyvex.fastprint.ui.components.IconPill
import com.notzyvex.fastprint.ui.components.PillSwitch
import com.notzyvex.fastprint.ui.components.SectionLabel
import com.notzyvex.fastprint.ui.components.Segment
import com.notzyvex.fastprint.ui.components.SegmentedControl
import com.notzyvex.fastprint.ui.components.Tag
import com.notzyvex.fastprint.ui.components.VSpace
import com.notzyvex.fastprint.ui.icons.Lucide
import com.notzyvex.fastprint.ui.theme.LocalAppAccent
import com.notzyvex.fastprint.ui.theme.Organic
import com.notzyvex.fastprint.ui.theme.body
import com.notzyvex.fastprint.ui.theme.heading

@Composable
fun CustomizeScreen(
    settings: PrintSettings,
    widthText: String,
    heightText: String,
    transform: ImageTransform,
    bitmap: android.graphics.Bitmap?,
    adjustMode: Boolean,
    onBack: () -> Unit,
    onWidth: (String) -> Unit,
    onHeight: (String) -> Unit,
    onUnit: (PaperUnit) -> Unit,
    onPreset: (PaperPreset) -> Unit,
    onOrientation: (Orientation) -> Unit,
    onIncCopies: () -> Unit,
    onDecCopies: () -> Unit,
    onColor: (Boolean) -> Unit,
    onDpi: (Int) -> Unit,
    onMargins: (Margins) -> Unit,
    onScale: (Scale) -> Unit,
    onToggleDuplex: () -> Unit,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
    onToggleAdjust: () -> Unit,
    onOpenEditor: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onChoosePrinter: () -> Unit,
) {
    val accent = LocalAppAccent.current

    Box(Modifier.fillMaxSize().background(Organic.Bg)) {
        Column(Modifier.fillMaxSize()) {

            // ---- header ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Organic.Bg)
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconPill(Lucide.ChevronLeft, "Back", onClick = onBack)
                HSpace(12.dp)
                Column {
                    Text("Customize", style = heading(19))
                    Text(settings.docType.label, style = body(12, color = Organic.muted()))
                }
            }

            // ---- live preview ----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Organic.Bg, Organic.Surface)))
                    .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    SheetPreview(
                        settings = settings,
                        transform = transform,
                        bitmap = bitmap,
                        adjustMode = adjustMode,
                        onDrag = onDrag,
                        onDoubleTap = onOpenEditor,
                    ) {
                        if (adjustMode) {
                            Box(
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                                    .clip(CircleShape)
                                    .background(Organic.Neutral900.copy(alpha = 0.78f))
                                    .padding(horizontal = 12.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    "Drag to move · double-tap to edit",
                                    style = body(11, color = Color.White),
                                )
                            }
                            IconPill(
                                icon = Lucide.Check,
                                contentDescription = "Done adjusting",
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp),
                                size = 44.dp,
                                iconSize = 22.dp,
                                background = Organic.Accent2,
                                tint = Color.White,
                                onClick = onToggleAdjust,
                            )
                        }
                    }
                }

                VSpace(10.dp)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag(settings.sizeLabel(), Organic.Neutral100, Organic.Neutral800)
                    Tag(
                        "${settings.copies} ${settings.copiesWord()}",
                        Organic.Accent100,
                        Organic.Accent800,
                    )
                }

                VSpace(10.dp)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PillAction(
                        label = if (bitmap != null) "Replace image" else "Upload image",
                        icon = Lucide.Upload,
                        onClick = onPickImage,
                    )
                    if (bitmap != null) {
                        PillAction(
                            label = "Adjust",
                            icon = Lucide.Move,
                            selected = adjustMode,
                            onClick = onToggleAdjust,
                        )
                        IconPill(
                            icon = Lucide.Trash,
                            contentDescription = "Remove image",
                            background = Organic.Bg,
                            border = Organic.Divider,
                            iconSize = 17.dp,
                            onClick = onClearImage,
                        )
                    }
                }
            }

            // ---- controls ----
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Organic.Surface)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 120.dp),
            ) {
                SectionLabel("Paper size")
                Row(verticalAlignment = Alignment.Bottom) {
                    NumberField(
                        label = "Width",
                        value = widthText,
                        onValueChange = onWidth,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        Modifier.width(22.dp).height(56.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("×", style = body(15, color = Organic.muted(0.45f)))
                    }
                    NumberField(
                        label = "Height",
                        value = heightText,
                        onValueChange = onHeight,
                        modifier = Modifier.weight(1f),
                    )
                    HSpace(10.dp)
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(CircleShape)
                            .border(1.dp, Organic.Divider, CircleShape),
                    ) {
                        PaperUnit.entries.forEach { u ->
                            val on = settings.unit == u
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .background(if (on) accent.value else Color.Transparent)
                                    .clickable { onUnit(u) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                            ) {
                                Text(
                                    u.label,
                                    style = heading(13, if (on) Organic.Bg else Organic.Text),
                                )
                            }
                        }
                    }
                }

                VSpace(12.dp)
                FlowChips(presets = PaperPresets, onPreset = onPreset)

                VSpace(22.dp)
                SectionLabel("Orientation")
                SegmentedControl(
                    options = listOf(
                        Segment(Orientation.PORTRAIT, "Portrait"),
                        Segment(Orientation.LANDSCAPE, "Landscape"),
                    ),
                    selected = settings.orientation,
                    onSelect = onOrientation,
                )

                VSpace(22.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Copies", style = heading(15))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconPill(
                            icon = Lucide.Minus,
                            contentDescription = "Fewer copies",
                            background = Organic.Bg,
                            border = Organic.Divider,
                            iconSize = 18.dp,
                            onClick = onDecCopies,
                        )
                        Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                            Text("${settings.copies}", style = heading(22))
                        }
                        IconPill(
                            icon = Lucide.Plus,
                            contentDescription = "More copies",
                            background = accent.value,
                            tint = Organic.Bg,
                            iconSize = 18.dp,
                            onClick = onIncCopies,
                        )
                    }
                }

                VSpace(22.dp)
                SectionLabel("Color")
                SegmentedControl(
                    options = listOf(
                        Segment(true, "Full color"),
                        Segment(false, "Black & white"),
                    ),
                    selected = settings.color,
                    onSelect = onColor,
                )

                VSpace(22.dp)
                SectionLabel("Quality")
                SegmentedControl(
                    options = listOf(
                        Segment(150, "Draft", "150 dpi"),
                        Segment(300, "Normal", "300 dpi"),
                        Segment(600, "High", "600 dpi"),
                    ),
                    selected = settings.dpi,
                    onSelect = onDpi,
                )

                VSpace(22.dp)
                SectionLabel("Margins")
                SegmentedControl(
                    options = listOf(
                        Segment(Margins.NONE, "None"),
                        Segment(Margins.NORMAL, "Normal"),
                        Segment(Margins.WIDE, "Wide"),
                    ),
                    selected = settings.margins,
                    onSelect = onMargins,
                )

                VSpace(22.dp)
                SectionLabel("Scale")
                SegmentedControl(
                    options = listOf(
                        Segment(Scale.FIT, "Fit"),
                        Segment(Scale.FILL, "Fill"),
                        Segment(Scale.ACTUAL, "Actual"),
                    ),
                    selected = settings.scale,
                    onSelect = onScale,
                )

                VSpace(22.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("Two-sided", style = heading(15))
                        Text("Print on both sides", style = body(12, color = Organic.muted()))
                    }
                    PillSwitch(checked = settings.duplex, onToggle = onToggleDuplex)
                }
            }
        }

        // ---- pinned action ----
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.32f to Organic.Surface,
                    )
                )
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 26.dp),
        ) {
            AccentButton(
                text = "Choose printer",
                trailing = Lucide.ChevronRight,
                onClick = onChoosePrinter,
            )
        }
    }
}

@Composable
private fun PillAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    val accent = LocalAppAccent.current
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) accent.value else Organic.Bg)
            .border(
                1.dp,
                if (selected) Color.Transparent else Organic.Divider,
                CircleShape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            null,
            Modifier.size(17.dp),
            tint = if (selected) Organic.Bg else Organic.Text,
        )
        HSpace(7.dp)
        Text(label, style = heading(14, if (selected) Organic.Bg else Organic.Text))
    }
}

/** The preset chips wrap onto a second line on narrow screens. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowChips(presets: List<PaperPreset>, onPreset: (PaperPreset) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        presets.forEach { preset ->
            Chip(preset.label) { onPreset(preset) }
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(label, style = body(12, color = Organic.muted(0.7f)))
        VSpace(5.dp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = body(14),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Organic.Bg,
                unfocusedContainerColor = Organic.Bg,
                focusedBorderColor = LocalAppAccent.current.value,
                unfocusedBorderColor = Organic.Divider,
                cursorColor = LocalAppAccent.current.value,
            ),
            modifier = Modifier.fillMaxWidth().height(48.dp),
        )
    }
}
