package com.paydock.telemetry

/** The app's process lifecycle: [register] calls `onBackground` each time the app goes to the background. */
internal fun interface AppLifecycle {
    fun register(onBackground: () -> Unit)
}
