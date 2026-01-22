/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.extensions

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import java.net.Inet4Address
import java.net.NetworkInterface

@Suppress("ConstPropertyName", "unused", "UnusedReceiverParameter")
object ContextExt {
    const val Unknown = "unknown"
    const val Wlan = "wlan"
    const val UserName = "user_name"
    const val EmptySSID = "Unknown"
    const val EmptyIP = "0.0.0.0"
    const val EmptyAndroidID = "No Id"
    const val UnknownSSID = "<unknown ssid>"
    const val EmptyUser = "-"

    val Context.ANDROID_ID: String
        @SuppressLint("HardwareIds")
        get() = runCatching {
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrElse {
            EmptyAndroidID
        }

    val Context.wifiManager
        get() = getSystemService(Context.WIFI_SERVICE) as? WifiManager

    val Context.powerManager
        get() = getSystemService(POWER_SERVICE) as PowerManager

    val Context.connectivityManager
        get() = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    // todo deprecation
    @Suppress("DEPRECATION")
    val Context.allNetworks: Map<Network, NetworkCapabilities?>
        get() = connectivityManager?.allNetworks?.associate { n ->
            n to connectivityManager?.getNetworkCapabilities(n)
        } ?: emptyMap()

    val Context.currentPublicIP: String
        get() = NetworkInterface.getNetworkInterfaces()
            .toList()
            .asSequence()
            .filter { n ->
                n.isUp && !n.isLoopback
            }
            .flatMap { n ->
                n.inetAddresses.asSequence()
            }
            .firstOrNull { n ->
                n is Inet4Address && !n.isLoopbackAddress
            }?.hostAddress ?: "..."

    // todo deprecation
    @Suppress("DEPRECATION")
    val Context.currentWifiSSID: String
        get() = run {
            var ssid = wifiManager?.connectionInfo?.ssid?.replace("\"", "")
            if (ssid == null || ssid == UnknownSSID || ssid == Unknown) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val connectivityManager = getSystemService(
                        Context.CONNECTIVITY_SERVICE
                    ) as? ConnectivityManager
                    val network = connectivityManager?.activeNetwork
                    val capabilities = connectivityManager?.getNetworkCapabilities(network)
                    val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                    if (isWifi) {
                        val wifiInfo = capabilities.transportInfo as? WifiInfo
                        ssid = wifiInfo?.ssid?.replace("\"", "")
                    }
                }
            }
            if (ssid == null || ssid == "<unknown ssid>") EmptySSID else ssid
        }

    val Context.currentWifiIP: String
        get() = runCatching {
            NetworkInterface.getNetworkInterfaces()
                .toList()
                .firstOrNull { n ->
                    n.name.startsWith(Wlan) && n.isUp
                }
                ?.inetAddresses
                ?.toList()
                ?.filterIsInstance<Inet4Address>()
                ?.firstOrNull { n ->
                    n.isSiteLocalAddress
                }?.hostAddress
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull() ?: EmptyIP

    val Context.currentSystemUser: String
        get() = runCatching {
            Settings.Secure.getString(contentResolver, UserName) ?: EmptyUser
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull() ?: EmptyUser

    inline fun <reified T : Service> Context.startForeground() = runCatching {
        Intent(
            this,
            T::class.java
        ).also { intent ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }.onFailure { e ->
        e.printStackTrace()
    }

    inline fun <reified T : Service> Context.startService() = runCatching {
        Intent(
            this,
            T::class.java
        ).also { intent ->
            startService(intent)
        }
    }.onFailure { e ->
        e.printStackTrace()
    }

    inline fun <reified T : Service> Context.stopService() = runCatching {
        Intent(
            this,
            T::class.java
        ).also { intent ->
            stopService(intent)
        }
    }.onFailure { e ->
        e.printStackTrace()
    }

    inline fun <reified T> Context.intent(
        block: Intent.() -> Unit
    ): Intent = Intent(applicationContext, T::class.java).apply(block)
}
