package com.notzyvex.fastprint

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.provider.Settings
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

@CapacitorPlugin(name = "FastPrint")
class FastPrintPlugin : Plugin() {

    private val found = ConcurrentHashMap<String, JSObject>()
    private var listeners = mutableListOf<NsdManager.DiscoveryListener>()
    private val nsd: NsdManager
        get() = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    private val serviceTypes = listOf("_ipps._tcp.", "_ipp._tcp.", "_pdl-datastream._tcp.")

    @PluginMethod
    fun startScan(call: PluginCall) {
        stopAll()
        found.clear()
        serviceTypes.forEach { type ->
            val l = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(s: String) {}
                override fun onDiscoveryStopped(s: String) {}
                override fun onStartDiscoveryFailed(s: String, e: Int) {}
                override fun onStopDiscoveryFailed(s: String, e: Int) {}
                override fun onServiceLost(info: NsdServiceInfo) {
                    found.remove(info.serviceName + "@" + type)
                    emit()
                }

                override fun onServiceFound(info: NsdServiceInfo) {
                    resolve(info, type)
                }
            }
            listeners.add(l)
            runCatching { nsd.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, l) }
        }
        call.resolve()
    }

    private fun resolve(info: NsdServiceInfo, type: String) {
        runCatching {
            @Suppress("DEPRECATION")
            nsd.resolveService(info, object : NsdManager.ResolveListener {
                override fun onResolveFailed(i: NsdServiceInfo, e: Int) {}
                override fun onServiceResolved(i: NsdServiceInfo) {
                    @Suppress("DEPRECATION")
                    val host = i.host?.hostAddress ?: return
                    val o = JSObject()
                    o.put("id", i.serviceName + "@" + type)
                    o.put("name", i.serviceName)
                    o.put("host", host)
                    o.put("port", i.port)
                    o.put("secure", type.contains("ipps"))
                    o.put("raw", type.contains("pdl-datastream"))
                    found[i.serviceName + "@" + type] = o
                    emit()
                }
            })
        }
    }

    private fun emit() {
        val arr = JSArray()
        found.values.forEach { arr.put(it) }
        val data = JSObject()
        data.put("printers", arr)
        notifyListeners("printersFound", data)
    }

    @PluginMethod
    fun stopScan(call: PluginCall) {
        stopAll()
        call.resolve()
    }

    private fun stopAll() {
        listeners.forEach { runCatching { nsd.stopServiceDiscovery(it) } }
        listeners = mutableListOf()
    }

    @PluginMethod
    fun printDirect(call: PluginCall) {
        val host = call.getString("host") ?: return call.reject("host required")
        val port = call.getInt("port") ?: 631
        val raw = call.getBoolean("raw") ?: false
        val dataB64 = call.getString("data") ?: return call.reject("data required")
        val jobName = call.getString("jobName") ?: "Fast Print"
        val copies = call.getInt("copies") ?: 1

        thread {
            try {
                val bytes = android.util.Base64.decode(dataB64, android.util.Base64.DEFAULT)
                if (raw || port == 9100) {
                    repeat(copies) { sendRaw(host, port, bytes) }
                } else {
                    sendIpp(host, port, bytes, jobName, copies, call.getBoolean("secure") ?: false)
                }
                val ok = JSObject()
                ok.put("status", "done")
                notifyListeners("printResult", ok)
                call.resolve(ok)
            } catch (e: Exception) {
                val bad = JSObject()
                bad.put("status", "failed")
                bad.put("error", e.message ?: "Print failed")
                notifyListeners("printResult", bad)
                call.reject(e.message ?: "Print failed")
            }
        }
    }

    private fun sendRaw(host: String, port: Int, bytes: ByteArray) {
        Socket().use { s ->
            s.connect(InetSocketAddress(host, port), 10000)
            s.getOutputStream().apply {
                write(bytes)
                flush()
            }
        }
    }

    private fun sendIpp(
        host: String,
        port: Int,
        doc: ByteArray,
        jobName: String,
        copies: Int,
        secure: Boolean,
    ) {
        val scheme = if (secure) "https" else "http"
        val uri = "$scheme://$host:$port/ipp/print"
        val body = ByteArrayOutputStream()
        val out = DataOutputStream(body)

        out.writeByte(0x02); out.writeByte(0x00)
        out.writeShort(0x0002)
        out.writeInt(1)

        out.writeByte(0x01)
        ippAttr(out, 0x47, "attributes-charset", "utf-8")
        ippAttr(out, 0x48, "attributes-natural-language", "en")
        ippAttr(out, 0x45, "printer-uri", "ipp://$host:$port/ipp/print")
        ippAttr(out, 0x42, "requesting-user-name", "fastprint")
        ippAttr(out, 0x42, "job-name", jobName)
        ippAttr(out, 0x49, "document-format", "application/pdf")

        out.writeByte(0x02)
        out.writeByte(0x21)
        out.writeShort(6); out.write("copies".toByteArray())
        out.writeShort(4); out.writeInt(copies)

        out.writeByte(0x03)
        out.write(doc)
        out.flush()

        val conn = (URL(uri).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 10000
            readTimeout = 30000
            setRequestProperty("Content-Type", "application/ipp")
        }
        conn.outputStream.use { it.write(body.toByteArray()) }
        val code = conn.responseCode
        val reply = runCatching { conn.inputStream.readBytes() }.getOrDefault(ByteArray(0))
        conn.disconnect()
        if (code !in 200..299) throw IllegalStateException("Printer returned HTTP $code")
        if (reply.size >= 4) {
            val status = ((reply[2].toInt() and 0xFF) shl 8) or (reply[3].toInt() and 0xFF)
            if (status > 0x00FF) throw IllegalStateException("Printer rejected the job (0x%04X)".format(status))
        }
    }

    private fun ippAttr(out: DataOutputStream, tag: Int, name: String, value: String) {
        out.writeByte(tag)
        out.writeShort(name.length); out.write(name.toByteArray())
        out.writeShort(value.length); out.write(value.toByteArray())
    }

    @PluginMethod
    fun checkUpdate(call: PluginCall) {
        val current = call.getString("currentVersion") ?: "1.0.0"
        val repo = call.getString("repo") ?: "msaqibsultan86/fast-print"
        thread {
            try {
                val conn = (URL("https://api.github.com/repos/$repo/releases/latest").openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "FastPrint")
                }
                if (conn.responseCode !in 200..299) {
                    conn.disconnect()
                    return@thread call.resolve(JSObject().apply { put("available", false) })
                }
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                conn.disconnect()

                val tag = json.optString("tag_name").removePrefix("v").trim()
                var url: String? = null
                var size = 0L
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val a = assets.getJSONObject(i)
                        if (a.optString("name").endsWith(".apk", true)) {
                            url = a.optString("browser_download_url")
                            size = a.optLong("size")
                            break
                        }
                    }
                }
                val newer = compareVersions(tag, current) > 0 && url != null
                call.resolve(JSObject().apply {
                    put("available", newer)
                    put("version", tag)
                    put("notes", json.optString("body").trim())
                    put("url", url ?: "")
                    put("size", size)
                })
            } catch (e: Exception) {
                call.resolve(JSObject().apply { put("available", false) })
            }
        }
    }

    @PluginMethod
    fun canInstall(call: PluginCall) {
        val ok = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true
        call.resolve(JSObject().apply { put("value", ok) })
    }

    @PluginMethod
    fun openInstallSettings(call: PluginCall) {
        val i = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            .setData(Uri.parse("package:" + context.packageName))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
        call.resolve()
    }

    @PluginMethod
    fun downloadAndInstall(call: PluginCall) {
        val url = call.getString("url") ?: return call.reject("url required")
        thread {
            try {
                val apk = File(context.cacheDir, "update.apk")
                if (apk.exists()) apk.delete()
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "FastPrint")
                }
                val total = conn.contentLengthLong
                conn.inputStream.use { input ->
                    apk.outputStream().use { output ->
                        val buf = ByteArray(65536)
                        var written = 0L
                        while (true) {
                            val r = input.read(buf)
                            if (r <= 0) break
                            output.write(buf, 0, r)
                            written += r
                            if (total > 0) {
                                notifyListeners("downloadProgress", JSObject().apply {
                                    put("percent", (written * 100 / total).toInt())
                                })
                            }
                        }
                    }
                }
                conn.disconnect()
                notifyListeners("downloadProgress", JSObject().apply { put("percent", 100) })
                installApk(apk)
                call.resolve()
            } catch (e: Exception) {
                notifyListeners("updateFailed", JSObject().apply {
                    put("error", e.message ?: "Update failed")
                })
                call.reject(e.message ?: "Update failed")
            }
        }
    }

    private fun installApk(apk: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(context.packageName)
        val id = installer.createSession(params)
        installer.openSession(id).use { session ->
            apk.inputStream().use { input ->
                session.openWrite("apk", 0, apk.length()).use { o ->
                    input.copyTo(o)
                    session.fsync(o)
                }
            }
            val intent = Intent(context, InstallReceiver::class.java)
                .setAction("com.notzyvex.fastprint.INSTALL_RESULT")
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else 0
            session.commit(PendingIntent.getBroadcast(context, id, intent, flags).intentSender)
        }
    }

    private fun compareVersions(a: String, b: String): Int {
        val pa = a.split('.', '-')
        val pb = b.split('.', '-')
        for (i in 0 until maxOf(pa.size, pb.size)) {
            val x = pa.getOrNull(i)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
            val y = pb.getOrNull(i)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
            if (x != y) return x - y
        }
        return 0
    }
}
