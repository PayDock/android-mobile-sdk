package com.paydock.telemetry

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

/**
 * The SDK's own private storage file; opened lazily, on first use. [putString] writes with `commit()`, so a value
 * (the exit marker) is on disk before the call returns, even if the process dies right after; every caller is on the
 * telemetry worker.
 */
internal class SharedPreferencesKeyValueStore(private val context: Context) : KeyValueStore {

    private val preferences: SharedPreferences by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    override fun getString(key: String): String? = preferences.getString(key, null)

    @SuppressLint("ApplySharedPref") // on the worker, and the marker must survive a crash loop
    override fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).commit()
    }

    companion object {
        const val FILE_NAME = "paydock_telemetry"
    }
}
