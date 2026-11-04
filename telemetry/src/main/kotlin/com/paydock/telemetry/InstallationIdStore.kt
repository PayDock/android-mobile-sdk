package com.paydock.telemetry

/**
 * The installation ID (R_10): a random ID kept in the SDK's private storage, stable across restarts and new for
 * each install. Nothing is read or written until [getOrCreate].
 */
internal class InstallationIdStore(private val store: KeyValueStore, private val idGenerator: IdGenerator) {

    /** Reads the stored ID; writes a new random ID only when none exists. */
    fun getOrCreate(): String {
        store.getString(KEY)?.let { return it }
        val id = idGenerator.newId()
        store.putString(KEY, id)
        return id
    }

    companion object {
        const val KEY = "app_installation_id"
    }
}
