package com.paydock.telemetry

import com.paydock.telemetry.TelemetryTestFakes.MapKeyValueStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * MD-5 acceptance for the installation ID (R_10): stable across restarts, new for each install, and nothing written
 * before [InstallationIdStore.getOrCreate].
 */
internal class InstallationIdStoreTest {

    @Test
    fun sameBackingStore_newStoreInstances_giveTheSameId() {
        val backing = mutableMapOf<String, String>()

        val first = InstallationIdStore(MapKeyValueStore(backing), IdGenerator.RANDOM).getOrCreate()
        // "Restart": a new store instance over the same storage.
        val second = InstallationIdStore(MapKeyValueStore(backing), IdGenerator.RANDOM).getOrCreate()

        assertEquals(first, second)
    }

    @Test
    fun freshBackingStore_givesADifferentId() {
        val first = InstallationIdStore(MapKeyValueStore(), IdGenerator.RANDOM).getOrCreate()
        // "Install": empty storage.
        val second = InstallationIdStore(MapKeyValueStore(), IdGenerator.RANDOM).getOrCreate()

        assertNotEquals(first, second)
    }

    @Test
    fun nothingIsWritten_beforeGetOrCreate() {
        val keyValues = MapKeyValueStore()

        InstallationIdStore(keyValues, IdGenerator.RANDOM)

        assertTrue(keyValues.values.isEmpty())
        assertEquals(0, keyValues.writes)
    }

    @Test
    fun getOrCreate_writesOnceUnderItsKey_andThenOnlyReads() {
        val keyValues = MapKeyValueStore()
        val store = InstallationIdStore(keyValues, IdGenerator.RANDOM)

        val id = store.getOrCreate()
        repeat(3) { assertEquals(id, store.getOrCreate()) }

        assertEquals(mapOf("app_installation_id" to id), keyValues.values)
        assertEquals(1, keyValues.writes)
    }

    @Test
    fun getOrCreate_keepsAnExistingId() {
        val keyValues = MapKeyValueStore(mutableMapOf(InstallationIdStore.KEY to "existing-id"))

        assertEquals("existing-id", InstallationIdStore(keyValues, IdGenerator.RANDOM).getOrCreate())
        assertEquals(0, keyValues.writes)
    }

    @Test
    fun storageNames_areTheDesignedOnes() {
        assertEquals("app_installation_id", InstallationIdStore.KEY)
        assertEquals("paydock_telemetry", SharedPreferencesKeyValueStore.FILE_NAME)
    }
}
