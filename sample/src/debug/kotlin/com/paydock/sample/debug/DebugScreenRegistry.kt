package com.paydock.sample.debug

import com.paydock.sample.debug.telemetry.StandInProxy
import com.paydock.sample.debug.telemetry.TelemetryTriggersScreen

/** The debug-only screens the Config hub links to. A release build has none (its registry is empty). */
object DebugScreenRegistry {
    val screens: List<DebugScreen> = listOf(
        DebugScreen(
            route = "telemetry_triggers",
            title = "Telemetry triggers",
            content = { TelemetryTriggersScreen() }
        )
    )

    /** Called from `App.onCreate` before the SDK is built: installs the stand-in proxy's pass-through selector. */
    fun onAppCreate() {
        StandInProxy().install()
    }
}
