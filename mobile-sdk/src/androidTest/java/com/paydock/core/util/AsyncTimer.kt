package com.paydock.core.util

import java.util.Timer
import kotlin.concurrent.schedule

internal object AsyncTimer {
    var expired = false
    fun start(delay: Long = 1000) {
        expired = false
        Timer().schedule(delay) {
            expired = true
        }
    }
}