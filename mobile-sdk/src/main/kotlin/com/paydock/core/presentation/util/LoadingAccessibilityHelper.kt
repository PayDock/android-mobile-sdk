package com.paydock.core.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.paydock.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Configuration for loading accessibility announcements.
 *
 * @property initialAnnouncementDelayMs Delay in milliseconds before the first announcement.
 *                                      Defaults to 0 (immediate).
 * @property reAnnouncementIntervalMs Interval in milliseconds before re-announcement.
 *                                   Defaults to 5000ms (5 seconds).
 * @property reAnnounceOnce If true, only re-announce once. If false, re-announce periodically.
 *                         Defaults to false (periodic re-announcements).
 */
data class LoadingAccessibilityConfig(
    val initialAnnouncementDelayMs: Long = 0L,
    val reAnnouncementIntervalMs: Long = 5000L,
    val reAnnounceOnce: Boolean = false
)

/**
 * Modifier that handles accessibility announcements for loading states.
 *
 * This modifier uses `contentDescription` and a `liveRegion` to make screen readers
 * announce loading status changes. It is designed to be flexible for various loading scenarios.
 *
 * This modifier will:
 * 1. Announce the initial `loadingMessage` when `isLoading` becomes true (after `initialAnnouncementDelayMs`).
 * 2. If `stillLoadingMessage` is provided, it will be announced after `reAnnouncementIntervalMs`.
 * 3. The re-announcement can be configured to happen once or periodically.
 * 4. Announcements stop automatically when `isLoading` becomes false.
 *
 * **Important:** This modifier uses `Modifier.composed` to manage its internal state. This prevents
 * the parent composable from recomposing when the announcement text changes, which is crucial
 * for avoiding interruptions to animations (e.g., a `CircularProgressIndicator`).
 *
 * @param isLoading Whether the component is currently in a loading state.
 * @param loadingMessage The message to announce when loading starts.
 * @param stillLoadingMessage The optional message to announce for subsequent loading updates.
 *                            If null, no re-announcements will occur.
 * @param config Configuration for announcement timing and repetition behavior.
 * @return A [Modifier] with the specified accessibility semantics for loading.
 */
@Composable
fun Modifier.loadingAccessibility(
    isLoading: Boolean,
    loadingMessage: String,
    stillLoadingMessage: String? = null,
    config: LoadingAccessibilityConfig = LoadingAccessibilityConfig()
): Modifier {
    return this.composed {
        var accessibilityAnnouncement by remember { mutableStateOf<String?>(null) }

        // This effect runs once and observes state changes internally.
        LaunchedEffect(Unit) {
            // snapshotFlow converts Compose state reads into a Kotlin Flow
            snapshotFlow { isLoading }
                .collect { isCurrentlyLoading ->
                    if (!isCurrentlyLoading) {
                        accessibilityAnnouncement = null
                        return@collect
                    }

                    // Launch a new child coroutine for the announcement sequence.
                    // This gets automatically cancelled when `isCurrentlyLoading` becomes false.
                    coroutineScope {
                        launch {
                            // 1. Initial Announcement
                            delay(config.initialAnnouncementDelayMs)
                            accessibilityAnnouncement = loadingMessage

                            // 2. Subsequent Announcements
                            val stillLoadingText = stillLoadingMessage ?: return@launch
                            delay(config.reAnnouncementIntervalMs)

                            if (config.reAnnounceOnce) {
                                accessibilityAnnouncement = stillLoadingText
                            } else {
                                var toggle = false
                                while (true) { // This loop is broken when the parent coroutine is cancelled
                                    val announcement = if (toggle) stillLoadingText else "$stillLoadingText\u00A0"
                                    accessibilityAnnouncement = announcement
                                    toggle = !toggle
                                    delay(config.reAnnouncementIntervalMs)
                                }
                            }
                        }
                    }
                }
        }

        // Semantics modifier
        accessibilityAnnouncement?.let { announcement ->
            Modifier.semantics(mergeDescendants = false) {
                liveRegion = LiveRegionMode.Polite
                contentDescription = announcement
            }
        } ?: Modifier
    }
}

/**
 * Convenience modifier for button loading accessibility announcements.
 *
 * Button loaders announce "Loading" when the activity indicator appears and on tap.
 * No timer/re-announcements.
 *
 * This modifier also overrides the "disabled" state announcement when loading,
 * so TalkBack will only announce "Loading" instead of "Loading, Button Disabled".
 *
 * @param isLoading Whether the button is currently loading.
 * @param buttonText The text displayed on the button (for context).
 * @return A Modifier with appropriate accessibility semantics.
 */
@Composable
fun Modifier.buttonLoadingAccessibility(
    isLoading: Boolean,
    buttonText: String? = null
): Modifier {
    val context = LocalContext.current
    val loadingMessage = context.getString(R.string.accessibility_button_loading)

    // Use a custom implementation that combines contentDescription with state override
    var accessibilityAnnouncement by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            accessibilityAnnouncement = loadingMessage
        } else {
            accessibilityAnnouncement = null
        }
    }

    return this.composed {
        val announcement = accessibilityAnnouncement
        if (announcement != null && isLoading) {
            Modifier.semantics(mergeDescendants = false) {
                // Set contentDescription to announce "Loading"
                liveRegion = LiveRegionMode.Polite
                contentDescription = announcement
                // Clear stateDescription to prevent "Button Disabled" announcement
                // Setting it to empty string should override the default disabled state
                stateDescription = ""
            }
        } else {
            Modifier
        }
    }
}

/**
 * Convenience modifier for webview loading accessibility announcements.
 *
 * WebView loaders announce "Loading" as soon as they show and on tap.
 * No timer/re-announcements.
 *
 * @param isLoading Whether the webview is currently loading.
 * @return A Modifier with appropriate accessibility semantics.
 */
@Composable
fun Modifier.webviewLoadingAccessibility(
    isLoading: Boolean
): Modifier {
    val context = LocalContext.current
    val loadingMessage = context.getString(R.string.accessibility_webview_loading)

    return loadingAccessibility(
        isLoading = isLoading,
        loadingMessage = loadingMessage,
        stillLoadingMessage = null, // No re-announcements for webviews
        config = LoadingAccessibilityConfig(
            initialAnnouncementDelayMs = 0L,
            reAnnouncementIntervalMs = Long.MAX_VALUE, // Effectively disable re-announcements
            reAnnounceOnce = false
        )
    )
}

/**
 * Convenience modifier for full screen overlay loading accessibility announcements.
 *
 * Full screen overlay loaders:
 * - Announce "Loading, please wait." immediately when shown
 * - After 10 seconds, announce "Still loading, this might take a few moments." (once only)
 * - On tap, announce "Loading, please wait."
 *
 * @param isLoading Whether the overlay is currently showing.
 * @return A Modifier with appropriate accessibility semantics.
 */
@Composable
fun Modifier.overlayLoadingAccessibility(
    isLoading: Boolean
): Modifier {
    val context = LocalContext.current
    val loadingMessage = context.getString(R.string.accessibility_overlay_loading)
    val stillLoadingMessage = context.getString(R.string.accessibility_overlay_still_loading)

    return loadingAccessibility(
        isLoading = isLoading,
        loadingMessage = loadingMessage,
        stillLoadingMessage = stillLoadingMessage,
        config = LoadingAccessibilityConfig(
            initialAnnouncementDelayMs = 0L,
            reAnnouncementIntervalMs = 10000L, // 10 seconds
            reAnnounceOnce = true // Only announce once after 10 seconds
        )
    )
}
