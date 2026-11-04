package com.paydock.telemetry

/** Makes random identifiers: session IDs and installation IDs. Never a hardware or advertising identifier. */
internal fun interface IdGenerator {
    fun newId(): String

    companion object {
        val RANDOM: IdGenerator = IdGenerator { java.util.UUID.randomUUID().toString() }
    }
}
