package com.paydock.telemetry

/** The records one prune removed, notices excluded: too old (156 h), or for space. */
internal data class PruneResult(val tooOld: Int = 0, val storeFull: Int = 0)
