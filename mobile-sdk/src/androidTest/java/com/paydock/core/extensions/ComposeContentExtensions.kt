package com.paydock.core.extensions

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.paydock.core.util.AsyncTimer

internal fun ComposeContentTestRule.waitUntilTimeout(
    timeoutMillis: Long
) {
    AsyncTimer.start(timeoutMillis)
    this.waitUntil(
        condition = { AsyncTimer.expired },
        timeoutMillis = timeoutMillis + 1000
    )
}