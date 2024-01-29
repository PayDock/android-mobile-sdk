/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
class ConnectivityManagerNetworkMonitor constructor(
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