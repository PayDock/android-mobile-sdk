package com.paydock.telemetry

/**
 * Installs one [ChainedCrashHandler] per process (RS-3: only an active start calls it). The first install chains the
 * slot's current handler; every later install only sets the handler's recorder, so a second SDK initialisation records
 * into its own session and never adds a second link. Synchronized.
 */
internal class CrashHandlerInstaller(private val slot: DefaultHandlerSlot) {

    /** The one link this installer put in the slot; null until the first install succeeds. */
    private var installed: ChainedCrashHandler? = null

    @Synchronized
    fun install(recorder: CrashRecorder) {
        val current = installed
        if (current != null) {
            current.recorder = recorder
            return
        }
        val handler = ChainedCrashHandler(previous = slot.get(), recorder = recorder)
        slot.set(handler)
        installed = handler
    }

    companion object {
        val PROCESS: CrashHandlerInstaller = CrashHandlerInstaller(DefaultHandlerSlot.SYSTEM)
    }
}
