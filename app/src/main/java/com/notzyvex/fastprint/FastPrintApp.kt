package com.notzyvex.fastprint

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notzyvex.fastprint.state.AppViewModel
import com.notzyvex.fastprint.state.Screen
import com.notzyvex.fastprint.ui.components.FastPrintToast
import com.notzyvex.fastprint.ui.screens.CustomizeScreen
import com.notzyvex.fastprint.ui.screens.DoneScreen
import com.notzyvex.fastprint.ui.screens.FailedScreen
import com.notzyvex.fastprint.ui.screens.HistoryScreen
import com.notzyvex.fastprint.ui.screens.HomeScreen
import com.notzyvex.fastprint.ui.screens.ImageEditorScreen
import com.notzyvex.fastprint.ui.screens.LaunchScreen
import com.notzyvex.fastprint.ui.screens.PrintersScreen
import com.notzyvex.fastprint.ui.screens.PrintingScreen
import com.notzyvex.fastprint.ui.screens.SettingsScreen
import com.notzyvex.fastprint.ui.screens.SignInScreen
import com.notzyvex.fastprint.ui.screens.UpdateScreen
import com.notzyvex.fastprint.ui.screens.WelcomeScreen
import com.notzyvex.fastprint.ui.theme.Organic

@Composable
fun FastPrintApp(vm: AppViewModel) {
    val context = LocalContext.current
    val activity = context as Activity

    val screen by vm.screen.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val widthText by vm.widthText.collectAsStateWithLifecycle()
    val heightText by vm.heightText.collectAsStateWithLifecycle()
    val transform by vm.transform.collectAsStateWithLifecycle()
    val bitmap by vm.bitmap.collectAsStateWithLifecycle()
    val adjustMode by vm.adjustMode.collectAsStateWithLifecycle()
    val editorOpen by vm.editorOpen.collectAsStateWithLifecycle()
    val authMode by vm.authMode.collectAsStateWithLifecycle()
    val profile by vm.profile.collectAsStateWithLifecycle()
    val signInError by vm.signInError.collectAsStateWithLifecycle()
    val accentTheme by vm.accent.collectAsStateWithLifecycle()
    val notifications by vm.notifications.collectAsStateWithLifecycle()
    val printers by vm.printers.collectAsStateWithLifecycle()
    val selectedPrinter by vm.selectedPrinter.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val failureReason by vm.failureReason.collectAsStateWithLifecycle()
    val availableUpdate by vm.availableUpdate.collectAsStateWithLifecycle()
    val updateStage by vm.updateStage.collectAsStateWithLifecycle()
    val downloadPercent by vm.downloadPercent.collectAsStateWithLifecycle()
    val updateError by vm.updateError.collectAsStateWithLifecycle()
    val toast by vm.toast.collectAsStateWithLifecycle()

    // Coming back from the "allow installs" settings screen should resume the update.
    val installSettings = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { vm.onReturnedFromInstallSettings() }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { vm.onImagePicked(it) } }

    // Android 13+ gates NsdManager discovery behind NEARBY_WIFI_DEVICES. Ask for it when the
    // user actually reaches the printer list, not on first launch, so the prompt has context.
    val requestNearby = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* discovery simply stays empty if denied; the system dialog still lists printers */ }

    LaunchedEffect(screen) {
        if (screen == Screen.PRINTERS &&
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
        ) {
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.NEARBY_WIFI_DEVICES,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) requestNearby.launch(android.Manifest.permission.NEARBY_WIFI_DEVICES)
        }
    }

    // System back mirrors each screen's own back affordance.
    BackHandler(enabled = screen != Screen.HOME && screen != Screen.LAUNCH) {
        when {
            editorOpen -> vm.cancelEditor()
            screen == Screen.CUSTOMIZE -> vm.go(Screen.HOME)
            screen == Screen.PRINTERS -> vm.go(Screen.CUSTOMIZE)
            screen == Screen.SETTINGS -> vm.go(Screen.HOME)
            screen == Screen.HISTORY -> vm.go(Screen.HOME)
            screen == Screen.DONE -> vm.go(Screen.HOME)
            screen == Screen.FAILED -> vm.go(Screen.PRINTERS)
            else -> Unit
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Organic.Bg)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        when (screen) {
            Screen.LAUNCH -> LaunchScreen(onSkip = vm::skipLaunch)

            Screen.UPDATE -> {
                val pending = availableUpdate
                if (pending == null) {
                    // Nothing to show — fall through rather than render an empty screen.
                    LaunchScreen(onSkip = vm::skipLaunch)
                } else {
                    UpdateScreen(
                        stage = updateStage,
                        update = pending,
                        currentVersion = BuildConfig.VERSION_NAME,
                        downloadPercent = downloadPercent,
                        errorMessage = updateError,
                        onUpdateNow = vm::startUpdate,
                        onAllowInstalls = {
                            installSettings.launch(vm.installPermissionIntent())
                        },
                        onNotNow = vm::backToAvailable,
                        onCancelDownload = vm::cancelDownload,
                        onRetry = vm::retryUpdate,
                        onDismiss = vm::dismissUpdate,
                    )
                }
            }

            Screen.SIGNIN -> SignInScreen(
                errorMessage = signInError,
                onGoogle = { vm.signInWithGoogle(activity) },
                onGuest = vm::continueAsGuest,
                onDismissError = vm::dismissSignInError,
            )

            Screen.WELCOME -> WelcomeScreen(profile = profile)

            Screen.HOME -> HomeScreen(
                onPickFormat = vm::pickFormat,
                onHistory = { vm.go(Screen.HISTORY) },
                onSettings = { vm.go(Screen.SETTINGS) },
            )

            Screen.CUSTOMIZE -> CustomizeScreen(
                settings = settings,
                widthText = widthText,
                heightText = heightText,
                transform = transform,
                bitmap = bitmap,
                adjustMode = adjustMode,
                onBack = { vm.go(Screen.HOME) },
                onWidth = vm::setWidthText,
                onHeight = vm::setHeightText,
                onUnit = vm::setUnit,
                onPreset = vm::applyPreset,
                onOrientation = vm::setOrientation,
                onIncCopies = vm::incCopies,
                onDecCopies = vm::decCopies,
                onColor = vm::setColor,
                onDpi = vm::setDpi,
                onMargins = vm::setMargins,
                onScale = vm::setScale,
                onToggleDuplex = vm::toggleDuplex,
                onPickImage = {
                    pickImage.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onClearImage = vm::clearImage,
                onToggleAdjust = vm::toggleAdjust,
                onOpenEditor = vm::openEditor,
                onDrag = vm::nudge,
                onChoosePrinter = { vm.go(Screen.PRINTERS) },
            )

            Screen.PRINTERS -> PrintersScreen(
                printers = printers,
                selected = selectedPrinter,
                onBack = { vm.go(Screen.CUSTOMIZE) },
                onSelect = vm::selectPrinter,
                onPrint = { vm.startPrint(activity) },
            )

            Screen.PRINTING -> PrintingScreen(
                settings = settings,
                printer = selectedPrinter ?: "your printer",
            )

            Screen.DONE -> DoneScreen(
                settings = settings,
                printer = selectedPrinter ?: "your printer",
                onPrintAgain = { vm.go(Screen.CUSTOMIZE) },
                onHome = { vm.go(Screen.HOME) },
            )

            Screen.FAILED -> FailedScreen(
                printer = selectedPrinter ?: "The printer",
                reason = failureReason,
                onRetry = { vm.startPrint(activity) },
                onChoosePrinter = { vm.go(Screen.PRINTERS) },
            )

            Screen.SETTINGS -> SettingsScreen(
                authMode = authMode,
                profile = profile,
                printer = selectedPrinter,
                notifications = notifications,
                accentTheme = accentTheme,
                versionName = BuildConfig.VERSION_NAME,
                updateReady = availableUpdate != null,
                onBack = { vm.go(Screen.HOME) },
                onToggleNotifications = vm::toggleNotifications,
                onAccent = vm::setAccent,
                onCheckUpdates = vm::checkForUpdates,
                onLogout = vm::logout,
                onSignIn = { vm.go(Screen.SIGNIN) },
            )

            Screen.HISTORY -> HistoryScreen(
                jobs = history,
                onBack = { vm.go(Screen.HOME) },
                onReprint = vm::reprint,
                onDelete = vm::deleteJob,
                onClearAll = vm::clearHistory,
            )
        }

        // The editor sits above whatever screen is showing.
        val editable = bitmap
        if (editorOpen && editable != null) {
            ImageEditorScreen(
                settings = settings,
                transform = transform,
                bitmap = editable,
                onCancel = vm::cancelEditor,
                onReset = vm::resetTransform,
                onZoom = vm::setZoom,
                onRotate = vm::rotate90,
                onApply = vm::applyEditor,
                onDrag = vm::nudge,
            )
        }

        FastPrintToast(toast)
    }
}
