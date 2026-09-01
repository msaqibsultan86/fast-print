package com.notzyvex.fastprint.print

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.print.PrintJob
import android.print.PrintJobInfo
import android.print.PrintManager
import android.util.Log
import com.notzyvex.fastprint.state.ImageTransform
import com.notzyvex.fastprint.state.PrintSettings

private const val TAG = "PrintController"
private const val POLL_INTERVAL_MS = 400L
private const val POLL_TIMEOUT_MS = 120_000L

/** The real outcome of a print job, as reported by the print framework. */
sealed interface PrintOutcome {
    data object Completed : PrintOutcome
    data object Cancelled : PrintOutcome
    data class Failed(val reason: String?) : PrintOutcome
    /**
     * The spooler handed the job to the print service and stopped reporting on it.
     * Treated as success — the sheet is on its way and no failure was ever raised.
     */
    data object HandedOff : PrintOutcome
}

/**
 * Starts an Android print job and watches it to a terminal state.
 *
 * Note this is the point where the OS takes over printer selection: Android has no public API
 * to preselect a printer or to read back which one the user chose (PrintJobInfo.getPrinterId is
 * a hidden SystemApi). The Printers screen is therefore the user's own saved preference, and
 * the system print dialog remains the authority on where the job actually lands.
 */
class PrintController(private val activity: Activity) {

    private val handler = Handler(Looper.getMainLooper())

    fun print(
        settings: PrintSettings,
        transform: ImageTransform,
        bitmap: Bitmap?,
        jobName: String,
        onOutcome: (PrintOutcome) -> Unit,
    ) {
        val printManager =
            activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            onOutcome(PrintOutcome.Failed("Printing is not available on this device"))
            return
        }

        val adapter = FastPrintDocumentAdapter(activity, settings, transform, bitmap)

        val job = try {
            printManager.print(jobName, adapter, settings.printAttributes())
        } catch (e: Exception) {
            Log.e(TAG, "PrintManager.print threw", e)
            onOutcome(PrintOutcome.Failed(e.message))
            return
        }

        watch(job, System.currentTimeMillis(), onOutcome)
    }

    private fun watch(job: PrintJob, startedAt: Long, onOutcome: (PrintOutcome) -> Unit) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                val state = runCatching { job.info?.state }.getOrNull()

                when (state) {
                    PrintJobInfo.STATE_COMPLETED -> onOutcome(PrintOutcome.Completed)

                    PrintJobInfo.STATE_FAILED ->
                        onOutcome(PrintOutcome.Failed(job.info?.label))

                    PrintJobInfo.STATE_CANCELED -> onOutcome(PrintOutcome.Cancelled)

                    // Blocked means the printer raised a condition — out of paper, jam, no toner.
                    PrintJobInfo.STATE_BLOCKED ->
                        onOutcome(PrintOutcome.Failed("The printer reported a problem"))

                    null -> onOutcome(PrintOutcome.HandedOff)

                    else -> {
                        if (System.currentTimeMillis() - startedAt > POLL_TIMEOUT_MS) {
                            onOutcome(PrintOutcome.HandedOff)
                        } else {
                            handler.postDelayed(this, POLL_INTERVAL_MS)
                        }
                    }
                }
            }
        }, POLL_INTERVAL_MS)
    }

    fun dispose() {
        handler.removeCallbacksAndMessages(null)
    }
}
