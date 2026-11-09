package com.paydock.telemetry

import android.app.ActivityManager
import android.app.Application
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.concurrent.atomic.AtomicBoolean

/**
 * API 30+: ActivityManager.getHistoricalProcessExitReasons(context.packageName, 0, 0), kept to entries whose
 * processName equals Application.getProcessName(), mapped to [ProcessExit] (REASON_ANR -> ANR, REASON_CRASH_NATIVE ->
 * CRASH_NATIVE, the five kill reasons by name, everything else, REASON_CRASH included, -> OTHER). Answers once per
 * process: every later call, from any instance, returns empty.
 *
 * Not unit-tested (no Robolectric): it only maps platform values; the rules live in [ExitReportReader].
 */
@RequiresApi(Build.VERSION_CODES.R)
internal class AndroidExitInfoSource(private val context: Context) : ExitInfoSource {

    override fun exits(): List<ProcessExit> {
        if (!answered.compareAndSet(false, true)) return emptyList()
        val manager = context.getSystemService(ActivityManager::class.java) ?: return emptyList()
        val processName = Application.getProcessName()
        return manager.getHistoricalProcessExitReasons(context.packageName, 0, 0)
            .filter { it.processName == processName }
            .map { info ->
                ProcessExit(
                    reason = reasonOf(info.reason),
                    foreground = info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND,
                    timestamp = info.timestamp,
                    trace = { info.traceInputStream }
                )
            }
    }

    companion object {
        /** Process-wide: the OS's list is read once per process, whichever instance asks first. */
        private val answered = AtomicBoolean(false)

        /** [AndroidExitInfoSource] on API 30+, [ExitInfoSource.NONE] below. */
        fun create(context: Context): ExitInfoSource = create(context, Build.VERSION.SDK_INT)

        /** Application.getProcessName() on API 30+ (only ever called there), for [ExitReportReader.markerKeyFor]. */
        @RequiresApi(Build.VERSION_CODES.R)
        fun processName(): String = Application.getProcessName()

        /** The version branch of [create], for tests. */
        internal fun create(context: Context, sdkInt: Int): ExitInfoSource =
            if (sdkInt >= Build.VERSION_CODES.R) AndroidExitInfoSource(context) else ExitInfoSource.NONE

        private fun reasonOf(reason: Int): ExitReason = when (reason) {
            ApplicationExitInfo.REASON_ANR -> ExitReason.ANR
            ApplicationExitInfo.REASON_CRASH_NATIVE -> ExitReason.CRASH_NATIVE
            ApplicationExitInfo.REASON_LOW_MEMORY -> ExitReason.LOW_MEMORY
            ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> ExitReason.EXCESSIVE_RESOURCE_USAGE
            ApplicationExitInfo.REASON_SIGNALED -> ExitReason.SIGNALED
            ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> ExitReason.INITIALIZATION_FAILURE
            ApplicationExitInfo.REASON_DEPENDENCY_DIED -> ExitReason.DEPENDENCY_DIED
            else -> ExitReason.OTHER
        }
    }
}
