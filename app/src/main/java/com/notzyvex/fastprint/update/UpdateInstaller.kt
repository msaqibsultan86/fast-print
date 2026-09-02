package com.notzyvex.fastprint.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext

private const val TAG = "UpdateInstaller"

/**
 * Downloads a release APK and hands it to the platform installer.
 *
 * Uses the PackageInstaller session API rather than an ACTION_VIEW intent on a FileProvider
 * URI: it needs no exported provider, streams straight into the session, and reports a real
 * result back instead of leaving us guessing.
 *
 * Android still shows its own confirmation dialog — an app cannot install silently unless it
 * is a device owner. The update must also be signed with the SAME key as the installed build
 * or the platform rejects it with INSTALL_FAILED_UPDATE_INCOMPATIBLE.
 */
class UpdateInstaller(private val context: Context) {

    /** API 26+ makes the user allow this app to install apps before we can do anything. */
    fun canInstall(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }

    /** The system screen where that permission is granted. */
    fun unknownSourcesIntent(): Intent =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            .setData(Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /**
     * Streams the APK to the cache dir, reporting 0..1 progress, then commits an install
     * session. Cancelling the calling coroutine aborts the download and deletes the partial file.
     */
    suspend fun download(
        update: AvailableUpdate,
        onProgress: (Float) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        val target = File(context.cacheDir, "update-${update.versionName}.apk")
        if (target.exists()) target.delete()

        val conn = (URL(update.apkUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "FastPrint")
        }

        try {
            if (conn.responseCode !in 200..299) {
                throw IllegalStateException("Download failed (HTTP ${conn.responseCode})")
            }
            val total = update.sizeBytes.takeIf { it > 0 }
                ?: conn.contentLengthLong.takeIf { it > 0 }
                ?: -1L

            conn.inputStream.use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var written = 0L
                    while (true) {
                        coroutineContext.ensureActive()
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        written += read
                        if (total > 0) onProgress((written.toFloat() / total).coerceIn(0f, 1f))
                    }
                    output.flush()
                }
            }
            onProgress(1f)
            target
        } catch (e: CancellationException) {
            target.delete()
            throw e
        } catch (e: Exception) {
            target.delete()
            throw e
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Commits the downloaded APK. Android then raises its confirmation dialog via
     * [InstallResultReceiver], which is why this returns as soon as the session is handed over.
     */
    suspend fun install(apk: File) = withContext(Dispatchers.IO) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(
            PackageInstaller.SessionParams.MODE_FULL_INSTALL
        ).apply { setAppPackageName(context.packageName) }

        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            apk.inputStream().use { input ->
                session.openWrite("fastprint.apk", 0, apk.length()).use { output ->
                    input.copyTo(output)
                    session.fsync(output)
                }
            }

            val intent = Intent(context, InstallResultReceiver::class.java)
                .setAction(InstallResultReceiver.ACTION_INSTALL_RESULT)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else {
                0
            }
            val pending = PendingIntent.getBroadcast(context, sessionId, intent, flags)
            session.commit(pending.intentSender)
            Log.i(TAG, "install session $sessionId committed")
        }
    }

    fun clearCachedApks() {
        runCatching {
            context.cacheDir.listFiles()
                ?.filter { it.name.startsWith("update-") && it.name.endsWith(".apk") }
                ?.forEach { it.delete() }
        }
    }
}
