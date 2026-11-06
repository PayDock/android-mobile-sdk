package com.paydock.telemetry

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * The process lifecycle of `ProcessLifecycleOwner`: [register] posts `addObserver` to the main looper, which the
 * lifecycle requires, and `onStop` calls `onBackground`. Every step is wrapped: a registration that fails, off the
 * main thread or in a JVM test, costs only the background flush; the 10 s flush still runs.
 */
internal object ProcessAppLifecycle : AppLifecycle {

    @Suppress("TooGenericExceptionCaught", "SwallowedException") // a failed registration only loses the background flush
    override fun register(onBackground: () -> Unit) {
        try {
            Handler(Looper.getMainLooper()).post { observe(onBackground) }
        } catch (ignored: Throwable) {
            // No main looper (a JVM test) or no lifecycle: the timer flush still runs.
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun observe(onBackground: () -> Unit) {
        try {
            ProcessLifecycleOwner.get().lifecycle.addObserver(BackgroundObserver(onBackground))
        } catch (ignored: Throwable) {
            // The lifecycle could not be observed: the timer flush still runs.
        }
    }

    private class BackgroundObserver(private val onBackground: () -> Unit) : DefaultLifecycleObserver {
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        override fun onStop(owner: LifecycleOwner) {
            try {
                onBackground()
            } catch (ignored: Throwable) {
                // Never let telemetry fail the app's lifecycle dispatch.
            }
        }
    }
}
