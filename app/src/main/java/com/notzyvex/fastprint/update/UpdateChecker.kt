package com.notzyvex.fastprint.update

import android.util.Log
import com.notzyvex.fastprint.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private const val TAG = "UpdateChecker"

/**
 * Fast Print ships as a sideloaded APK rather than through Play, so GitHub Releases is the
 * update feed: publish a release tagged v<versionName> with the APK attached and every
 * install picks it up on next launch.
 */
private const val RELEASES_API =
    "https://api.github.com/repos/msaqibsultan86/fast-print/releases/latest"

private const val TIMEOUT_MS = 10_000

/** A published release newer than the running build. */
data class AvailableUpdate(
    val versionName: String,
    val notes: String,
    val apkUrl: String,
    val sizeBytes: Long,
) {
    /** "12.4 MB" for the download-size line. */
    val readableSize: String
        get() = when {
            sizeBytes <= 0 -> "unknown size"
            sizeBytes >= 1024 * 1024 ->
                String.format(Locale.US, "%.1f MB", sizeBytes / (1024.0 * 1024.0))
            else -> String.format(Locale.US, "%.0f KB", sizeBytes / 1024.0)
        }
}

sealed interface UpdateCheck {
    data class Available(val update: AvailableUpdate) : UpdateCheck
    data object UpToDate : UpdateCheck
    data class Failed(val reason: String) : UpdateCheck
}

class UpdateChecker(private val currentVersionName: String = BuildConfig.VERSION_NAME) {

    suspend fun check(): UpdateCheck = withContext(Dispatchers.IO) {
        try {
            val body = fetch() ?: return@withContext UpdateCheck.Failed("No response")
            val json = JSONObject(body)

            if (json.optBoolean("draft") || json.optBoolean("prerelease")) {
                return@withContext UpdateCheck.UpToDate
            }

            val tag = json.optString("tag_name").removePrefix("v").trim()
            if (tag.isEmpty()) return@withContext UpdateCheck.Failed("Release has no tag")
            if (compareVersions(tag, currentVersionName) <= 0) {
                return@withContext UpdateCheck.UpToDate
            }

            var apkUrl: String? = null
            var size = 0L
            json.optJSONArray("assets")?.let { assets ->
                for (i in 0 until assets.length()) {
                    val a = assets.getJSONObject(i)
                    if (a.optString("name").endsWith(".apk", ignoreCase = true)) {
                        apkUrl = a.optString("browser_download_url")
                        size = a.optLong("size")
                        break
                    }
                }
            }

            val url = apkUrl
            if (url.isNullOrBlank()) {
                return@withContext UpdateCheck.Failed("That release has no APK attached")
            }

            UpdateCheck.Available(
                AvailableUpdate(
                    versionName = tag,
                    notes = json.optString("body").trim(),
                    apkUrl = url,
                    sizeBytes = size,
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "update check failed", e)
            UpdateCheck.Failed(e.message ?: "Could not reach the update server")
        }
    }

    private fun fetch(): String? {
        val conn = (URL(RELEASES_API).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "FastPrint/${BuildConfig.VERSION_NAME}")
        }
        return try {
            if (conn.responseCode !in 200..299) {
                Log.w(TAG, "update check HTTP ${conn.responseCode}")
                null
            } else {
                conn.inputStream.bufferedReader().use { it.readText() }
            }
        } finally {
            conn.disconnect()
        }
    }
}

/**
 * Returns >0 when [a] is newer than [b].
 *
 * Compares numerically per segment so 1.0.10 correctly beats 1.0.9, which a plain string
 * compare gets wrong. Missing segments count as 0, so "1.1" equals "1.1.0".
 */
internal fun compareVersions(a: String, b: String): Int {
    val pa = a.split('.', '-', '+')
    val pb = b.split('.', '-', '+')
    for (i in 0 until maxOf(pa.size, pb.size)) {
        val x = pa.getOrNull(i)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
        val y = pb.getOrNull(i)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
        if (x != y) return x - y
    }
    return 0
}
