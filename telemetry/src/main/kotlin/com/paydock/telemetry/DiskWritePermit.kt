package com.paydock.telemetry

import android.os.StrictMode

/** Runs a block with disk reads and writes allowed on the calling thread, behind a port for JVM tests. */
internal fun interface DiskWritePermit {
    fun allow(block: () -> Unit)

    companion object {
        /**
         * StrictMode.allowThreadDiskWrites() (which also allows reads), the previous policy restored in `finally`.
         * When StrictMode itself cannot be read (a JVM without the Android runtime), the block still runs.
         */
        val STRICT_MODE: DiskWritePermit = DiskWritePermit { block ->
            val previous = runCatching { StrictMode.allowThreadDiskWrites() }.getOrNull()
            try {
                block()
            } finally {
                if (previous != null) runCatching { StrictMode.setThreadPolicy(previous) }
            }
        }

        val NONE: DiskWritePermit = DiskWritePermit { it() } // tests
    }
}
