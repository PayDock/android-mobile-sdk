package com.paydock.core.data.injection.modules

import com.paydock.MobileSDK
import com.paydock.core.telemetry.NetworkFailureReporting
import com.paydock.core.telemetry.RecordingTelemetry
import com.paydock.core.telemetry.defaultTelemetryModule
import com.paydock.telemetry.NoOpTelemetry
import com.paydock.telemetry.Telemetry
import io.ktor.client.HttpClient
import io.ktor.client.plugins.pluginOrNull
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/**
 * The guarded install (R_07, RS-8): the network module's API client carries the network failure plugin only when the
 * bound facade is active; with NoOpTelemetry (the default module, or a failed collector URL lookup) it has none, and
 * the BIN client never has it. Each test resolves the clients from its own Koin instance.
 */
internal class NetworkModuleTelemetryTest {

    private lateinit var koin: Koin

    @Before
    fun setUp() {
        val sdk = mockk<MobileSDK>()
        every { sdk.baseUrl } returns "api-sandbox.paydock.com"
        MobileSDK.registerInstance(sdk)
    }

    @After
    fun tearDown() {
        koin.close()
        MobileSDK.reset()
    }

    private fun koinWith(telemetry: Telemetry?): Koin {
        val telemetryModule = telemetry?.let { bound -> module { single<Telemetry> { bound } } } ?: defaultTelemetryModule
        return koinApplication {
            allowOverride(true)
            modules(defaultTelemetryModule, networkModule, telemetryModule)
        }.koin.also { koin = it }
    }

    @Test
    fun withNoOpTelemetry_theApiClientHasNoPlugin() {
        val koin = koinWith(telemetry = null)

        assertSame(NoOpTelemetry, koin.get<Telemetry>())
        assertNull(koin.get<HttpClient>().pluginOrNull(NetworkFailureReporting))
        assertNull(koin.get<HttpClient>(binDataHttpClientQualifier).pluginOrNull(NetworkFailureReporting))
    }

    @Test
    fun withAnInactiveFacade_theApiClientHasNoPlugin() {
        val koin = koinWith(RecordingTelemetry(isActive = false))

        assertNull(koin.get<HttpClient>().pluginOrNull(NetworkFailureReporting))
    }

    @Test
    fun withAnActiveFacade_theApiClientHasThePlugin_andTheBinClientDoesNot() {
        val koin = koinWith(RecordingTelemetry())

        assertNotNull(koin.get<HttpClient>().pluginOrNull(NetworkFailureReporting))
        assertNull(koin.get<HttpClient>(binDataHttpClientQualifier).pluginOrNull(NetworkFailureReporting))
    }
}
