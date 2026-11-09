package com.paydock.telemetry

import android.content.ContextWrapper
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [SharedPreferencesKeyValueStore] over a recording [SharedPreferences] (no Robolectric). Review fix
 * (R5): a write is committed, never applied, so the exit marker is on disk before the call returns.
 */
internal class SharedPreferencesKeyValueStoreTest {

    /** Preferences over a plain map that log every commit and apply. */
    private class RecordingPreferences : SharedPreferences {
        val values = mutableMapOf<String, String>()
        val log = mutableListOf<String>()

        override fun getString(key: String, defValue: String?): String? = values[key] ?: defValue
        override fun edit(): SharedPreferences.Editor = RecordingEditor()
        override fun getAll(): Map<String, *> = values.toMap()
        override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? = defValues
        override fun getInt(key: String, defValue: Int): Int = defValue
        override fun getLong(key: String, defValue: Long): Long = defValue
        override fun getFloat(key: String, defValue: Float): Float = defValue
        override fun getBoolean(key: String, defValue: Boolean): Boolean = defValue
        override fun contains(key: String): Boolean = key in values
        override fun registerOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener,
        ) = Unit

        override fun unregisterOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener,
        ) = Unit

        inner class RecordingEditor : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, String>()

            override fun putString(key: String, value: String?): SharedPreferences.Editor = apply {
                if (value != null) pending[key] = value
            }

            override fun commit(): Boolean {
                log += "commit"
                values += pending
                return true
            }

            override fun apply() {
                log += "apply"
                values += pending
            }

            override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor = this
            override fun putInt(key: String, value: Int): SharedPreferences.Editor = this
            override fun putLong(key: String, value: Long): SharedPreferences.Editor = this
            override fun putFloat(key: String, value: Float): SharedPreferences.Editor = this
            override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = this
            override fun remove(key: String): SharedPreferences.Editor = this
            override fun clear(): SharedPreferences.Editor = this
        }
    }

    private val preferences = RecordingPreferences()
    private val opened = mutableListOf<String>()

    private val context = object : ContextWrapper(null) {
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
            opened += name
            return preferences
        }
    }

    @Test
    fun putString_commits_andNeverApplies() {
        val store = SharedPreferencesKeyValueStore(context)

        store.putString(ExitReportReader.MARKER_KEY, "1700000000000")

        assertEquals(listOf("commit"), preferences.log)
        assertEquals("1700000000000", preferences.values[ExitReportReader.MARKER_KEY])
    }

    @Test
    fun getString_readsWhatWasCommitted_fromThePrivateFile() {
        val store = SharedPreferencesKeyValueStore(context)
        assertNull(store.getString("absent"))

        store.putString("key", "value")

        assertEquals("value", store.getString("key"))
        assertEquals(listOf(SharedPreferencesKeyValueStore.FILE_NAME), opened)
    }
}
