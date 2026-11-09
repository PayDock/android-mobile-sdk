package com.paydock.telemetry

/** The ApplicationExitInfo reasons the reader acts on; every other reason is OTHER. The name is paydock.crash.reason. */
internal enum class ExitReason {
    ANR,
    CRASH_NATIVE,
    LOW_MEMORY,
    EXCESSIVE_RESOURCE_USAGE,
    SIGNALED,
    INITIALIZATION_FAILURE,
    DEPENDENCY_DIED,
    OTHER,
}
