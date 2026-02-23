package com.paydock.binprocessor.data.refresh

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.paydock.binprocessor.data.cache.BinDataCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * App-scoped coordinator that triggers BIN data refresh so the merchant always checks
 * against the latest BIN list. Refresh runs:
 * - At SDK initialisation
 * - On app foregrounding when there is no cached file
 * - On app foregrounding when cached file exists and last fetch was more than 1 hour ago
 *
 * All BIN network requests (HEAD/GET) use the same HTTP client and thus go through the
 * configured proxy for both fresh app launch and foregrounding.
 *
 * @param binDataCacheManager Cache manager that performs HEAD/GET and persistence
 * @param scope Coroutine scope for launching refresh (application-scoped)
 * @param registerLifecycle Whether to register with ProcessLifecycleOwner (false for tests)
 */
class BinDataRefreshCoordinator internal constructor(
    private val binDataCacheManager: BinDataCacheManager,
    private val scope: CoroutineScope,
    registerLifecycle: Boolean = true
) : DefaultLifecycleObserver {

    init {
        if (registerLifecycle) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (binDataCacheManager.shouldRefreshOnForeground()) {
            scope.launch {
                binDataCacheManager.refreshBinDataIfNeeded()
            }
        }
    }

    /**
     * Triggers a one-off BIN refresh at SDK initialization (non-blocking).
     * Uses the same HEAD-then-conditional-GET flow as foreground refresh.
     */
    fun refreshAtSdkInit() {
        scope.launch {
            binDataCacheManager.refreshBinDataIfNeeded()
        }
    }

    /**
     * Cancels all pending refresh operations. Used during testing to clean up
     * background coroutines before SDK reset.
     */
    @VisibleForTesting
    fun cancelPendingRefresh() {
        scope.cancel()
    }

    companion object {
        /**
         * Creates an application-scoped CoroutineScope with [SupervisorJob] and [Dispatchers.Default].
         * Used by the coordinator for fire-and-forget refresh work.
         */
        fun applicationScope(): CoroutineScope =
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
