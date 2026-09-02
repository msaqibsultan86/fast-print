package com.notzyvex.fastprint.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log

private const val TAG = "InstallResult"

/**
 * Receives the PackageInstaller session outcome.
 *
 * STATUS_PENDING_USER_ACTION is the normal path: the platform hands back a confirmation intent
 * that must be launched so the user can approve the install. Everything else is terminal, and
 * the failure text is stashed for the Update screen to show.
 */
class InstallResultReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_INSTALL_RESULT) return

        when (val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                @Suppress("DEPRECATION")
                val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                if (confirm != null) {
                    confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirm)
                } else {
                    Log.w(TAG, "pending user action with no confirmation intent")
                    lastFailure = "Android did not show the install dialog"
                }
            }

            PackageInstaller.STATUS_SUCCESS -> {
                Log.i(TAG, "update installed")
                lastFailure = null
            }

            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Log.w(TAG, "install failed status=$status message=$message")
                lastFailure = message ?: "Install failed (code $status)"
            }
        }
    }

    companion object {
        const val ACTION_INSTALL_RESULT = "com.notzyvex.fastprint.INSTALL_RESULT"

        /** Read by the Update screen to explain a failure. Best effort, not authoritative. */
        @Volatile
        var lastFailure: String? = null
    }
}
