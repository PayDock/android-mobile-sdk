package com.paydock.telemetry

/** A private string key-value store. */
internal interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}
