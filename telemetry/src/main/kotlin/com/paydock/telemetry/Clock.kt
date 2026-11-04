package com.paydock.telemetry

/** The current time as a UTC epoch count in milliseconds (R_11). Never local time or a zone. */
internal fun interface Clock {
    fun now(): Long // UTC epoch milliseconds; never local time or a time zone

    companion object {
        val SYSTEM: Clock = Clock { System.currentTimeMillis() }
    }
}
