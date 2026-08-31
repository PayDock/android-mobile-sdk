package com.paydock.feature.card.presentation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * A handle for driving [com.paydock.feature.card.presentation.GiftCardWidget]'s submission from
 * outside the widget — for use when `GiftCardWidgetConfig.showSubmitButton` is `false` and a host
 * app supplies its own submit UI.
 *
 * Create one with [rememberGiftCardWidgetState] and pass it to `GiftCardWidget`'s `state` parameter.
 * Call [submit] from your own button's `onClick` to trigger validation and tokenisation — this runs
 * the identical path the widget's own (hidden) button would have run, including error announcements
 * and focus-to-first-error, and is a no-op while a request is already in flight. [isFormValid] is
 * the form's raw validity; it is **not** gated on `GiftCardWidgetConfig.activePrimaryButton`. To
 * drive your own button's enabled state, branch on the same `activePrimaryButton` value passed into
 * the config: `true` means the internal button would stay enabled and validate on tap, so your
 * button should do the same; `false` means it would stay disabled until valid, so gate your button
 * on [isFormValid] instead. A submitting/loading signal is already available via `loadingDelegate`
 * (`widgetLoadingDidStart()`/`widgetLoadingDidFinish()`) — no separate signal is exposed here.
 */
@Stable
class GiftCardWidgetState internal constructor() {

    /**
     * Whether the gift card form currently passes validation. Updated live as the user types.
     */
    var isFormValid: Boolean by mutableStateOf(false)
        internal set

    private val _submitRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    internal val submitRequests: SharedFlow<Unit> = _submitRequests.asSharedFlow()

    /**
     * Requests that the widget validate and, if valid, tokenise the gift card — equivalent to
     * tapping the widget's own submit button. A no-op while a request is already in flight.
     */
    fun submit() {
        _submitRequests.tryEmit(Unit)
    }
}

/**
 * Creates and remembers a [GiftCardWidgetState] for the current composition.
 */
@Composable
fun rememberGiftCardWidgetState(): GiftCardWidgetState = remember { GiftCardWidgetState() }
