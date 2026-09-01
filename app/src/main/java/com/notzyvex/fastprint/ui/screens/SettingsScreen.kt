package com.notzyvex.fastprint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.notzyvex.fastprint.state.AuthMode
import com.notzyvex.fastprint.state.UserProfile
import com.notzyvex.fastprint.ui.components.AccentButton
import com.notzyvex.fastprint.ui.components.HSpace
import com.notzyvex.fastprint.ui.components.IconPill
import com.notzyvex.fastprint.ui.components.OutlineButton
import com.notzyvex.fastprint.ui.components.PillSwitch
import com.notzyvex.fastprint.ui.components.Segment
import com.notzyvex.fastprint.ui.components.SegmentedControl
import com.notzyvex.fastprint.ui.components.Tag
import com.notzyvex.fastprint.ui.components.VSpace
import com.notzyvex.fastprint.ui.icons.Lucide
import com.notzyvex.fastprint.ui.theme.AccentTheme
import com.notzyvex.fastprint.ui.theme.LocalAppAccent
import com.notzyvex.fastprint.ui.theme.Organic
import com.notzyvex.fastprint.ui.theme.body
import com.notzyvex.fastprint.ui.theme.heading

@Composable
fun SettingsScreen(
    authMode: AuthMode,
    profile: UserProfile,
    printer: String?,
    notifications: Boolean,
    accentTheme: AccentTheme,
    versionName: String,
    onBack: () -> Unit,
    onToggleNotifications: () -> Unit,
    onAccent: (AccentTheme) -> Unit,
    onLogout: () -> Unit,
    onSignIn: () -> Unit,
) {
    val accent = LocalAppAccent.current
    val isGoogle = authMode == AuthMode.GOOGLE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Organic.Bg)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 40.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconPill(Lucide.ChevronLeft, "Back", onClick = onBack)
            HSpace(12.dp)
            Text("Settings", style = heading(22))
        }

        VSpace(22.dp)

        // ---- profile card ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Organic.Surface)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGoogle) {
                            Brush.linearGradient(listOf(accent.value, Organic.Accent2))
                        } else {
                            Brush.linearGradient(listOf(Organic.Neutral500, Organic.Neutral500))
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isGoogle && profile.photoUrl != null -> AsyncImage(
                        model = profile.photoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )

                    isGoogle -> Text(profile.initials, style = heading(32, Color.White))

                    else -> Icon(Lucide.User, null, Modifier.size(40.dp), tint = Color.White)
                }
            }

            VSpace(12.dp)
            Text(
                if (isGoogle) profile.name ?: "Signed in" else "Guest",
                style = heading(21),
            )
            Text(
                if (isGoogle) profile.email ?: "" else "Not signed in",
                style = body(13, color = Organic.muted()),
            )
            VSpace(12.dp)
            if (isGoogle) {
                Tag("Google account", Organic.Accent2100, Organic.Accent2800)
            } else {
                Tag("Guest session", Organic.Neutral200, Organic.Neutral800)
            }
        }

        VSpace(18.dp)

        // ---- settings list ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Organic.Surface),
        ) {
            SettingRow(icon = Lucide.Printer, iconTint = Organic.Accent700, label = "Default printer") {
                Text(
                    printer ?: "Not set",
                    style = body(13, color = Organic.muted()),
                )
            }
            Divider()
            SettingRow(icon = Lucide.Bell, iconTint = Organic.Accent2800, label = "Notifications") {
                PillSwitch(checked = notifications, onToggle = onToggleNotifications)
            }
            Divider()
            SettingRow(icon = Lucide.Info, iconTint = Organic.Accent700, label = "Version") {
                Text(versionName, style = body(13, color = Organic.muted()))
            }
        }

        VSpace(18.dp)

        Text("Theme", style = heading(15))
        VSpace(10.dp)
        SegmentedControl(
            options = listOf(
                Segment(AccentTheme.Terracotta, "Terracotta"),
                Segment(AccentTheme.Sage, "Sage"),
            ),
            selected = accentTheme,
            onSelect = onAccent,
        )

        VSpace(22.dp)

        if (isGoogle) {
            OutlineButton(
                text = "Log out",
                leading = Lucide.LogOut,
                contentColor = Organic.DangerText,
                borderColor = Organic.Danger.copy(alpha = 0.45f),
                onClick = onLogout,
            )
        } else {
            AccentButton(
                text = "Sign in to your account",
                minHeight = 52.dp,
                fontSize = 15,
                onClick = onSignIn,
            )
        }
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, null, Modifier.size(20.dp), tint = iconTint)
            HSpace(13.dp)
            Text(label, style = body(15))
        }
        trailing()
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Organic.Divider)
    )
}
