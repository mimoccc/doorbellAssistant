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
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.mjdev.phone.data.User
import java.io.File
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
    const val EmptyUser = "Unknown"

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

    @Suppress("DEPRECATION")
    val Context.currentWifiSSID: String
        get() = run {
            var ssid: String? = null
            try {
                val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                ssid = wifiManager.connectionInfo?.ssid
                Log.d("WiFi", "Old method result: $ssid")
            } catch (e: Exception) {
                Log.e("WiFi", "Old method failed: ${e.message}")
            }
            if (ssid != null && ssid != "\"<unknown ssid>\"" && ssid != "<unknown ssid>") {
                return@run ssid.replace("\"", "")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val network = connectivityManager.activeNetwork
                    val capabilities = connectivityManager.getNetworkCapabilities(network)
                    if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                        val wifiInfo = capabilities.transportInfo as? WifiInfo
                        ssid = wifiInfo?.ssid
                        Log.d("WiFi", "New method result: $ssid")
                    }
                } catch (e: Exception) {
                    Log.e("WiFi", "New method failed: ${e.message}")
                }
            }
            ssid = ssid?.replace("\"", "")
            when {
                ssid.isNullOrBlank() -> EmptySSID
                ssid == "<unknown ssid>" -> EmptySSID
                else -> ssid
            }
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

    val Context.currentSystemUserName: String
        get() = runCatching {
            Settings.Secure.getString(applicationContext.contentResolver, UserName) ?: EmptyUser
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrNull() ?: EmptyUser

    const val USER_PREFS = "user_prefs"
    const val USER_PREFS_NAME = "user_prefs_name"
    const val USER_PREFS_PIC = "user_prefs_pic"

    fun Context.persistPickedImage(uri: Uri): Uri = try {
        if (uri != Uri.EMPTY && uri.scheme == "content") {
            val input = contentResolver.openInputStream(uri)
            val file = File(filesDir, "profile_pic.jpg")
            if (input != null) {
                file.outputStream().use { output ->
                    input.copyTo(output)
                    output.flush()
                }
                Uri.fromFile(file)
            } else {
                Uri.EMPTY
            }
        } else {
            Uri.EMPTY
        }
    } catch (e: Exception) {
        Log.e("persistPickedImage", "Failed to copy image", e)
        Uri.EMPTY
    }

    fun Context.getDeviceUser(): Flow<User?> = flow {
        val uri = ContactsContract.Profile.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_URI
        )
        val cursor = applicationContext.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )
        var user: User? = null
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val nameCol = c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
                val picCol = c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI)
                val name = c.getString(nameCol)
                val photoUri = c.getString(picCol)
                user = User(name, photoUri ?: "")
            }
        }
        if (user == null) {
            val prefs = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
            val name = prefs.getString(USER_PREFS_NAME, null)
            val pic = prefs.getString(USER_PREFS_PIC, null)
            user = if (name != null && pic != null) {
                User(name, pic)
            } else if (name != null) {
                User(name)
            } else {
                User()
            }
        }
        emit(user)
    }.flowOn(Dispatchers.IO)

    fun Context.saveSystemProfile(
        userName: String?,
        userPic: Uri?,
        onSave: () -> Unit = {}
    ) {
        val prefs = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
        val oldPic = prefs.getString(USER_PREFS_PIC, null)?.toUri()
        val localUri = userPic?.let { uri ->
            val pickedUri = persistPickedImage(uri)
            if (pickedUri == Uri.EMPTY) oldPic else pickedUri
        } ?: oldPic ?: Uri.EMPTY
        prefs.edit {
            if (userName?.isNotEmpty() == true) {
                putString(USER_PREFS_NAME, userName)
            }
            if (localUri != null && localUri != Uri.EMPTY) {
                putString(USER_PREFS_PIC, localUri.toString())
            }
        }
        onSave()
    }

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
