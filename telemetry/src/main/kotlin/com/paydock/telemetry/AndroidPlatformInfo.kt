package com.paydock.telemetry

import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * The Android [PlatformInfo]: OS version, manufacturer, model and the merchant app's ID, version and debug flag.
 * Every string falls back to `""` and the connection type to [NetworkConnectionType.UNKNOWN] on any failure. It
 * reads no hardware or advertising identifier (principle 10).
 */
internal class AndroidPlatformInfo(private val context: Context) : PlatformInfo {

    override val osVersion: String
        get() = safeString { Build.VERSION.RELEASE }

    override val manufacturer: String
        get() = safeString { Build.MANUFACTURER }

    override val model: String
        get() = safeString { Build.MODEL }

    override val appId: String
        get() = safeString { context.packageName }

    @Suppress("DEPRECATION") // the int-flags overload is the one available down to minSdk 24
    override val appVersion: String
        get() = safeString { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "" }

    override val appDebuggable: Boolean
        get() = runCatching { (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0 }
            .getOrDefault(false)

    override fun networkConnectionType(): NetworkConnectionType = runCatching {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivity.activeNetwork ?: return@runCatching NetworkConnectionType.NONE
        val capabilities = connectivity.getNetworkCapabilities(network)
        when {
            capabilities == null -> NetworkConnectionType.UNKNOWN
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkConnectionType.CELL
            else -> NetworkConnectionType.UNKNOWN
        }
    }.getOrDefault(NetworkConnectionType.UNKNOWN)

    private inline fun safeString(read: () -> String?): String = runCatching { read() ?: "" }.getOrDefault("")
}
