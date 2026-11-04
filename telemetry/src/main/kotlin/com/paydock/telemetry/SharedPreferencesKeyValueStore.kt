package com.paydock.telemetry

import android.content.Context
import android.content.SharedPreferences

/** The SDK's own private storage file; opened lazily, on first use. */
internal class SharedPreferencesKeyValueStore(private val context: Context) : KeyValueStore {

    private val preferences: SharedPreferences by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    companion object {
        const val FILE_NAME = "paydock_telemetry"
    }
}
