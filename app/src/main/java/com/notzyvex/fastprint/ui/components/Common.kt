package com.notzyvex.fastprint.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.notzyvex.fastprint.ui.theme.LocalAppAccent
import com.notzyvex.fastprint.ui.theme.Organic
import com.notzyvex.fastprint.ui.theme.body
import com.notzyvex.fastprint.ui.theme.heading

/** Every button in this design is a pill. */
private val Pill = CircleShape

@Composable
fun AccentButton(
    text: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 52.dp,
    fontSize: Int = 16,
    enabled: Boolean = true,
    leading: ImageVector? = null,
    trailing: ImageVector? = null,
    onClick: () -> Unit,
) {
    val accent = LocalAppAccent.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .clip(Pill)
            .background(if (enabled) accent.value else accent.value.copy(alpha = 0.45f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.let {
            Icon(it, null, Modifier.size(18.dp).padding(end = 0.dp), tint = Organic.Bg)
            Box(Modifier.width(8.dp))
        }
        Text(text, style = heading(fontSize, Organic.Bg))
        trailing?.let {
            Box(Modifier.width(6.dp))
            Icon(it, null, Modifier.size(20.dp), tint = Organic.Bg)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 48.dp,
    fontSize: Int = 15,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .clip(Pill)
            .background(Organic.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = heading(fontSize))
    }
}

@Composable
fun OutlineButton(
    text: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 52.dp,
    fontSize: Int = 15,
    contentColor: Color = Organic.Text,
    borderColor: Color = Organic.Text.copy(alpha = 0.18f),
    leading: ImageVector? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .clip(Pill)
            .border(1.5.dp, borderColor, Pill)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.let {
            Icon(it, null, Modifier.size(19.dp), tint = contentColor)
            Box(Modifier.width(9.dp))
        }
        Text(text, style = heading(fontSize, contentColor))
    }
}

/** 36dp circular icon button — the header/stepper control. */
@Composable
fun IconPill(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 20.dp,
    background: Color = Organic.Surface,
    tint: Color = Organic.Text,
    border: Color? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .then(if (border != null) Modifier.border(1.dp, border, CircleShape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, Modifier.size(iconSize), tint = tint)
    }
}

/** One option in a [SegmentedControl]. [sub] renders the "150 dpi" second line. */
data class Segment<T>(val value: T, val label: String, val sub: String? = null)

@Composable
fun <T> SegmentedControl(
    options: List<Segment<T>>,
    selected: T,
    modifier: Modifier = Modifier,
    onSelect: (T) -> Unit,
) {
    val accent = LocalAppAccent.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Pill)
            .border(1.dp, Organic.Divider, Pill),
    ) {
        options.forEach { option ->
            val isOn = option.value == selected
            val bg by animateColorAsState(
                if (isOn) accent.value else Color.Transparent,
                label = "segment-bg",
            )
            CenteredColumn(
                modifier = Modifier
                    .weight(1f)
                    .background(bg)
                    .clickable { onSelect(option.value) }
                    .padding(horizontal = 8.dp, vertical = 11.dp),
            ) {
                Text(
                    option.label,
                    style = heading(13, if (isOn) Organic.Bg else Organic.Text),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                option.sub?.let {
                    Text(
                        it,
                        style = body(10, color = (if (isOn) Organic.Bg else Organic.Text).copy(alpha = 0.7f)),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** Stacks a segment's label over its sub-label, centred. */
@Composable
private fun CenteredColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) = Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    content = content,
)

/** The 52x30 pill toggle with a 24dp knob. */
@Composable
fun PillSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
) {
    val accent = LocalAppAccent.current
    val track by animateColorAsState(
        if (checked) accent.value else Organic.Neutral300,
        label = "switch-track",
    )
    val knobOffset by animateDpAsState(if (checked) 22.dp else 0.dp, label = "switch-knob")

    Box(
        modifier = modifier
            .size(width = 52.dp, height = 30.dp)
            .clip(Pill)
            .background(track)
            .clickable(onClick = onToggle)
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .padding(start = knobOffset)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Composable
fun Tag(
    text: String,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(text, style = body(11, color = contentColor))
    }
}

@Composable
fun Chip(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(Pill)
            .border(1.dp, Organic.Divider, Pill)
            .background(Organic.Bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 7.dp),
    ) {
        Text(text, style = heading(13))
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, style = heading(15), modifier = modifier.padding(bottom = 10.dp))
}

@Composable
fun HSpace(width: Dp) {
    Box(Modifier.width(width))
}

@Composable
fun VSpace(height: Dp) {
    Box(Modifier.height(height))
}
