package com.paydock.core.data.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest.Builder
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/**
 * Implementation of the [NetworkMonitor] interface that uses the Android ConnectivityManager
 * to monitor the network connectivity status.
 *
 * @param context The context used to access system services.
 */
internal class ConnectivityManagerNetworkMonitor(
    private val context: Context
) : NetworkMonitor {
    /**
     * Flow representing the network connectivity status.
     * Emits `true` when the device is online and `false` when it is offline.
     */
    override val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()

        // Check if the ConnectivityManager is available
        if (connectivityManager == null) {
            // Emit false to indicate offline status
            channel.trySend(false)
            channel.close()
            return@callbackFlow
        }

        // Callback to track network availability
        val callback = object : NetworkCallback() {
            private val networks = mutableSetOf<Network>()

            override fun onAvailable(network: Network) {
                networks += network
                channel.trySend(true)
            }

            override fun onLost(network: Network) {
                networks -= network
                channel.trySend(networks.isNotEmpty())
            }
        }

        // Network request to listen for connectivity changes
        val request = Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        // Register the network callback
        connectivityManager.registerNetworkCallback(request, callback)

        // Send the initial connectivity status to the channel
        channel.trySend(connectivityManager.isCurrentlyConnected())

        // Unregister the network callback when the flow is no longer collected
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .conflate()

    /**
     * Helper function to check if the ConnectivityManager is currently connected to the internet.
     *
     * @return `true` if the device is connected to the internet, `false` otherwise.
     */
    private fun ConnectivityManager.isCurrentlyConnected() = activeNetwork
        ?.let(::getNetworkCapabilities)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
}