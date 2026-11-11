package com.paydock.sample.debug

/** The debug-only screens of a release build: none. */
object DebugScreenRegistry {
    val screens: List<DebugScreen> = emptyList()

    /** Called from `App.onCreate` before the SDK is built: nothing to do in a release build. */
    fun onAppCreate() {}
}
