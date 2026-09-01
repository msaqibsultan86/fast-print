package com.notzyvex.fastprint.print

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "PrinterDiscovery"

/** A printer found on the local network via mDNS/DNS-SD. */
data class DiscoveredPrinter(
    val id: String,
    val name: String,
    val host: String?,
    val port: Int,
    val serviceType: String,
    /** From the TXT record where the printer publishes it; null when it says nothing. */
    val stateMessage: String? = null,
) {
    /** "Ready · Wi-Fi" style status line shown under the printer name. */
    val statusLine: String
        get() = stateMessage?.takeIf { it.isNotBlank() }?.let { "$it · Wi-Fi" } ?: "Ready · Wi-Fi"

    val isSecure: Boolean get() = serviceType.contains("ipps")
}

/**
 * Service types worth browsing. IPP/IPPS cover AirPrint and Mopria; pdl-datastream covers
 * legacy JetDirect-style raw-9100 printers that still advertise over Bonjour.
 */
private val SERVICE_TYPES = listOf(
    "_ipps._tcp.",
    "_ipp._tcp.",
    "_pdl-datastream._tcp.",
)

class PrinterDiscovery(context: Context) {

    private val nsdManager =
        context.applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager

    /**
     * Emits the running set of printers seen on the current Wi-Fi network.
     * Cancelling the collector tears every discovery listener down.
     */
    fun discover(): Flow<List<DiscoveredPrinter>> = callbackFlow {
        val found = ConcurrentHashMap<String, DiscoveredPrinter>()
        val listeners = mutableListOf<NsdManager.DiscoveryListener>()

        fun publish() {
            trySend(found.values.sortedBy { it.name.lowercase() })
        }

        fun onResolved(info: NsdServiceInfo, type: String) {
            @Suppress("DEPRECATION")
            val host = info.host?.hostAddress
            val txtState = info.attributes?.get("printer-state-reasons")
                ?.let { runCatching { String(it) }.getOrNull() }
                ?.takeIf { it.isNotBlank() && it != "none" }
                ?.replace('-', ' ')
                ?.replaceFirstChar { it.uppercase() }

            val key = "${info.serviceName}@$type"
            found[key] = DiscoveredPrinter(
                id = key,
                name = info.serviceName,
                host = host,
                port = info.port,
                serviceType = type,
                stateMessage = txtState,
            )
            publish()
        }

        SERVICE_TYPES.forEach { type ->
            val listener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(serviceType: String) {
                    Log.d(TAG, "discovery started for $serviceType")
                }

                override fun onServiceFound(service: NsdServiceInfo) {
                    resolve(service, type, ::onResolved)
                }

                override fun onServiceLost(service: NsdServiceInfo) {
                    found.remove("${service.serviceName}@$type")
                    publish()
                }

                override fun onDiscoveryStopped(serviceType: String) = Unit

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.w(TAG, "start discovery failed for $serviceType: $errorCode")
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.w(TAG, "stop discovery failed for $serviceType: $errorCode")
                }
            }
            listeners += listener
            runCatching {
                nsdManager.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, listener)
            }.onFailure { Log.w(TAG, "discoverServices($type) threw", it) }
        }

        publish()

        awaitClose {
            listeners.forEach { l ->
                runCatching { nsdManager.stopServiceDiscovery(l) }
            }
        }
    }

    /**
     * resolveService is deprecated from API 34 in favour of registerServiceInfoCallback,
     * which also keeps the record up to date instead of resolving once.
     */
    private fun resolve(
        service: NsdServiceInfo,
        type: String,
        onResolved: (NsdServiceInfo, String) -> Unit,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runCatching {
                nsdManager.registerServiceInfoCallback(
                    service,
                    { it.run() },
                    object : NsdManager.ServiceInfoCallback {
                        override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
                            Log.w(TAG, "info callback registration failed: $errorCode")
                        }

                        override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {
                            onResolved(serviceInfo, type)
                        }

                        override fun onServiceLost() = Unit
                        override fun onServiceInfoCallbackUnregistered() = Unit
                    },
                )
            }.onFailure { Log.w(TAG, "registerServiceInfoCallback threw", it) }
        } else {
            @Suppress("DEPRECATION")
            nsdManager.resolveService(
                service,
                object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.w(TAG, "resolve failed for ${serviceInfo.serviceName}: $errorCode")
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        onResolved(serviceInfo, type)
                    }
                },
            )
        }
    }
}
