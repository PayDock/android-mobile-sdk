package com.paydock.feature.threeDS.standalone.presentation.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.paydock.core.presentation.util.WidgetLoadingDelegate

/**
 * Tracks the loading state of the Standalone 3DS widget and keeps [WidgetLoadingDelegate]
 * notifications balanced.
 *
 * Loading starts when the widget launches ([start]); afterwards the delegate is only notified on actual
 * transitions ([update]), so it never receives two starts or two finishes in a row.
 */
internal class Standalone3DSLoadingController {

    /**
     * Whether the widget is currently loading. Backed by Compose state so the built-in overlay recomposes.
     */
    var isLoading by mutableStateOf(true)
        private set

    /**
     * Starts loading when the widget launches.
     *
     * @param delegate The optional loading delegate to notify.
     */
    fun start(delegate: WidgetLoadingDelegate?) {
        isLoading = true
        delegate?.widgetLoadingDidStart()
    }

    /**
     * Updates the loading state, notifying the delegate only if the state actually changes.
     *
     * @param loading `true` to start loading, `false` to finish.
     * @param delegate The optional loading delegate to notify.
     */
    fun update(loading: Boolean, delegate: WidgetLoadingDelegate?) {
        if (loading == isLoading) return
        isLoading = loading
        if (loading) {
            delegate?.widgetLoadingDidStart()
        } else {
            delegate?.widgetLoadingDidFinish()
        }
    }
}
