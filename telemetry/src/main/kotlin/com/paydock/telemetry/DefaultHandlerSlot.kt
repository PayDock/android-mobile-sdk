package com.paydock.telemetry

/** The JVM's default uncaught-exception handler, behind a port for tests. */
internal interface DefaultHandlerSlot {
    fun get(): Thread.UncaughtExceptionHandler?
    fun set(handler: Thread.UncaughtExceptionHandler)

    companion object {
        val SYSTEM: DefaultHandlerSlot = object : DefaultHandlerSlot {
            override fun get(): Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

            override fun set(handler: Thread.UncaughtExceptionHandler) {
                Thread.setDefaultUncaughtExceptionHandler(handler)
            }
        }
    }
}
