package com.notzyvex.fastprint.state

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.notzyvex.fastprint.R
import com.notzyvex.fastprint.auth.GoogleAuth
import com.notzyvex.fastprint.auth.SignInResult
import com.notzyvex.fastprint.data.HistoryRepository
import com.notzyvex.fastprint.data.ImageStore
import com.notzyvex.fastprint.data.JobStatus
import com.notzyvex.fastprint.data.Prefs
import com.notzyvex.fastprint.data.PrintJobEntity
import com.notzyvex.fastprint.print.DiscoveredPrinter
import com.notzyvex.fastprint.print.PrintController
import com.notzyvex.fastprint.print.PrintOutcome
import com.notzyvex.fastprint.print.PrinterDiscovery
import com.notzyvex.fastprint.ui.theme.AccentTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)
    private val images = ImageStore(app)
    private val repo = HistoryRepository(app)
    private val discovery = PrinterDiscovery(app)
    private val auth = GoogleAuth(app.getString(R.string.google_web_client_id))

    val googleConfigured: Boolean get() = auth.isConfigured

    // ---- navigation ----
    private val _screen = MutableStateFlow(Screen.LAUNCH)
    val screen = _screen.asStateFlow()

    // ---- print settings ----
    private val _settings = MutableStateFlow(PrintSettings())
    val settings = _settings.asStateFlow()

    /** Raw text for the W/H fields so a half-typed value does not snap back while editing. */
    private val _widthText = MutableStateFlow("8.5")
    val widthText = _widthText.asStateFlow()
    private val _heightText = MutableStateFlow("11")
    val heightText = _heightText.asStateFlow()

    // ---- image ----
    private val _imagePath = MutableStateFlow<String?>(null)
    val imagePath = _imagePath.asStateFlow()
    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap = _bitmap.asStateFlow()
    private val _transform = MutableStateFlow(ImageTransform())
    val transform = _transform.asStateFlow()
    private val _adjustMode = MutableStateFlow(false)
    val adjustMode = _adjustMode.asStateFlow()
    private val _editorOpen = MutableStateFlow(false)
    val editorOpen = _editorOpen.asStateFlow()
    private var editorSnapshot: ImageTransform = ImageTransform()

    // ---- auth ----
    private val _authMode = MutableStateFlow(prefs.authMode)
    val authMode = _authMode.asStateFlow()
    private val _profile = MutableStateFlow(
        UserProfile(prefs.displayName, prefs.email, prefs.photoUrl)
    )
    val profile = _profile.asStateFlow()
    private val _signInError = MutableStateFlow<String?>(null)
    val signInError = _signInError.asStateFlow()

    // ---- prefs ----
    private val _accent = MutableStateFlow(prefs.accent)
    val accent = _accent.asStateFlow()
    private val _notifications = MutableStateFlow(prefs.notifications)
    val notifications = _notifications.asStateFlow()

    // ---- printers ----
    private val _selectedPrinter = MutableStateFlow(prefs.defaultPrinter)
    val selectedPrinter = _selectedPrinter.asStateFlow()
    val printers: StateFlow<List<DiscoveredPrinter>> = discovery.discover()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---- history ----
    val history: StateFlow<List<PrintJobEntity>> = repo.jobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---- print outcome ----
    private val _failureReason = MutableStateFlow<String?>(null)
    val failureReason = _failureReason.asStateFlow()

    private var printController: PrintController? = null
    private var launchJob: Job? = null

    init {
        launchJob = viewModelScope.launch {
            delay(Timing.LAUNCH_MS)
            if (_screen.value == Screen.LAUNCH) {
                _screen.value =
                    if (_authMode.value == AuthMode.GOOGLE) Screen.HOME else Screen.SIGNIN
            }
        }
    }

    // ================= navigation =================
    fun go(target: Screen) {
        _screen.value = target
    }

    fun skipLaunch() {
        launchJob?.cancel()
        if (_screen.value == Screen.LAUNCH) _screen.value = Screen.SIGNIN
    }

    fun pickFormat(type: DocType) {
        val s = PrintSettings.forDocType(type)
        _settings.value = s
        _widthText.value = PrintSettings.fmt(s.width)
        _heightText.value = PrintSettings.fmt(s.height)
        _screen.value = Screen.CUSTOMIZE
    }

    // ================= auth =================
    fun continueAsGuest() {
        _authMode.value = AuthMode.GUEST
        prefs.authMode = AuthMode.GUEST
        _screen.value = Screen.HOME
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            when (val result = auth.signIn(activity)) {
                is SignInResult.Success -> {
                    _profile.value = result.profile
                    prefs.displayName = result.profile.name
                    prefs.email = result.profile.email
                    prefs.photoUrl = result.profile.photoUrl
                    _authMode.value = AuthMode.GOOGLE
                    prefs.authMode = AuthMode.GOOGLE
                    _signInError.value = null
                    _screen.value = Screen.WELCOME
                    delay(Timing.WELCOME_MS)
                    if (_screen.value == Screen.WELCOME) _screen.value = Screen.HOME
                }

                SignInResult.Cancelled -> Unit

                SignInResult.NoAccounts -> _signInError.value =
                    "No Google account on this device. Add one in Android Settings, or continue as guest."

                SignInResult.NotConfigured -> _signInError.value =
                    "Google Sign-In is not configured yet. See SETUP.md."

                is SignInResult.Error -> _signInError.value = result.message
            }
        }
    }

    fun dismissSignInError() {
        _signInError.value = null
    }

    fun logout() {
        _authMode.value = AuthMode.GUEST
        _profile.value = UserProfile()
        prefs.clearIdentity()
        _screen.value = Screen.SIGNIN
    }

    // ================= settings mutation =================
    fun update(block: (PrintSettings) -> PrintSettings) {
        _settings.value = block(_settings.value)
    }

    fun setWidthText(text: String) {
        _widthText.value = text
        text.toDoubleOrNull()?.takeIf { it > 0 }?.let { w -> update { it.copy(width = w) } }
    }

    fun setHeightText(text: String) {
        _heightText.value = text
        text.toDoubleOrNull()?.takeIf { it > 0 }?.let { h -> update { it.copy(height = h) } }
    }

    fun applyPreset(preset: PaperPreset) {
        update { it.copy(width = preset.w, height = preset.h, unit = preset.unit) }
        _widthText.value = PrintSettings.fmt(preset.w)
        _heightText.value = PrintSettings.fmt(preset.h)
    }

    fun setUnit(unit: PaperUnit) = update { it.copy(unit = unit) }
    fun setOrientation(o: Orientation) = update { it.copy(orientation = o) }
    fun setColor(color: Boolean) = update { it.copy(color = color) }
    fun setDpi(dpi: Int) = update { it.copy(dpi = dpi) }
    fun setMargins(m: Margins) = update { it.copy(margins = m) }
    fun setScale(s: Scale) = update { it.copy(scale = s) }
    fun toggleDuplex() = update { it.copy(duplex = !it.duplex) }
    fun incCopies() = update { it.copy(copies = (it.copies + 1).coerceAtMost(99)) }
    fun decCopies() = update { it.copy(copies = (it.copies - 1).coerceAtLeast(1)) }

    fun setAccent(theme: AccentTheme) {
        _accent.value = theme
        prefs.accent = theme
    }

    fun toggleNotifications() {
        val next = !_notifications.value
        _notifications.value = next
        prefs.notifications = next
    }

    fun selectPrinter(name: String) {
        _selectedPrinter.value = name
        prefs.defaultPrinter = name
    }

    // ================= image =================
    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            val path = images.importFrom(uri) ?: return@launch
            val previous = _imagePath.value
            _imagePath.value = path
            _bitmap.value = images.load(path)
            _transform.value = ImageTransform()
            // Only drop the old file once no history row still needs it.
            if (previous != null && history.value.none { it.imagePath == previous }) {
                images.delete(previous)
            }
        }
    }

    fun clearImage() {
        val previous = _imagePath.value
        _imagePath.value = null
        _bitmap.value = null
        _transform.value = ImageTransform()
        _adjustMode.value = false
        if (previous != null && history.value.none { it.imagePath == previous }) {
            images.delete(previous)
        }
    }

    /** Drag deltas arrive as a fraction of the frame, keeping the transform resolution-free. */
    fun nudge(dxFraction: Float, dyFraction: Float) {
        _transform.value = _transform.value.let {
            it.copy(offsetX = it.offsetX + dxFraction, offsetY = it.offsetY + dyFraction)
        }
    }

    fun setZoom(zoom: Float) {
        _transform.value = _transform.value.copy(zoom = zoom)
    }

    fun rotate90() {
        _transform.value = _transform.value.let { it.copy(rotation = (it.rotation + 90) % 360) }
    }

    fun resetTransform() {
        _transform.value = ImageTransform()
    }

    fun toggleAdjust() {
        _adjustMode.value = !_adjustMode.value
    }

    fun openEditor() {
        editorSnapshot = _transform.value
        _adjustMode.value = false
        _editorOpen.value = true
    }

    fun applyEditor() {
        _editorOpen.value = false
    }

    fun cancelEditor() {
        _transform.value = editorSnapshot
        _editorOpen.value = false
    }

    // ================= printing =================
    fun startPrint(activity: Activity) {
        val settings = _settings.value
        val transform = _transform.value
        val bmp = _bitmap.value
        val printerName = _selectedPrinter.value ?: "Default printer"

        _screen.value = Screen.PRINTING
        _failureReason.value = null

        printController?.dispose()
        val controller = PrintController(activity)
        printController = controller

        controller.print(
            settings = settings,
            transform = transform,
            bitmap = bmp,
            jobName = "Fast Print - ${settings.docType.label}",
        ) { outcome ->
            val status = when (outcome) {
                PrintOutcome.Completed, PrintOutcome.HandedOff -> JobStatus.DONE
                is PrintOutcome.Failed -> JobStatus.FAILED
                PrintOutcome.Cancelled -> null
            }

            if (status == null) {
                // The user dismissed the system print dialog - no job ran, so no history row.
                _screen.value = Screen.PRINTERS
                return@print
            }

            if (outcome is PrintOutcome.Failed) _failureReason.value = outcome.reason

            viewModelScope.launch {
                repo.add(
                    PrintJobEntity.from(
                        settings = settings,
                        transform = transform,
                        printer = printerName,
                        imagePath = _imagePath.value,
                        status = status,
                        createdAt = System.currentTimeMillis(),
                    )
                )
            }
            _screen.value = if (status == JobStatus.DONE) Screen.DONE else Screen.FAILED
        }
    }

    // ================= history =================
    fun reprint(job: PrintJobEntity) {
        viewModelScope.launch {
            _settings.value = job.toSettings()
            _widthText.value = PrintSettings.fmt(job.width)
            _heightText.value = PrintSettings.fmt(job.height)
            _transform.value = job.toTransform()
            _imagePath.value = job.imagePath
            _bitmap.value = images.load(job.imagePath)
            _screen.value = Screen.CUSTOMIZE
        }
    }

    fun deleteJob(job: PrintJobEntity) {
        viewModelScope.launch { repo.delete(job) }
    }

    fun clearHistory() {
        viewModelScope.launch { repo.clear() }
    }

    override fun onCleared() {
        printController?.dispose()
        printController = null
        super.onCleared()
    }
}
