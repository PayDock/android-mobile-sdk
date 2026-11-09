package com.paydock.telemetry

import java.io.InputStream

/** One exit of this process, as the OS reported it, without Android types. */
internal class ProcessExit(
    val reason: ExitReason,
    val foreground: Boolean, // importance at exit == IMPORTANCE_FOREGROUND
    val timestamp: Long, // UTC epoch ms of the exit
    private val trace: () -> InputStream?, // the OS's trace, or null; may throw
) {
    fun openTrace(): InputStream? = trace()
}
