package com.paydock.core.data.util

import kotlinx.coroutines.flow.Flow

/**
 * Utility for reporting connectivity status
 */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
