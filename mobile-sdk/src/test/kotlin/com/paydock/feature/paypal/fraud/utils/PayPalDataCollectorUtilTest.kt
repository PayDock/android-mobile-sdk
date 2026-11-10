package com.paydock.feature.paypal.fraud.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ProcessLifecycleOwner
import com.paydock.MobileSDK
import com.paydock.core.BaseUnitTest
import com.paydock.core.MobileSDKConstants
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.domain.error.exceptions.PayPalDataCollectorException
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.domain.injection.domainModule
import com.paydock.core.domain.model.Environment
import com.paydock.core.telemetry.RecordingTelemetry
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.paypal.core.domain.usecase.GetPayPalClientIdUseCase
import com.paydock.feature.paypal.fraud.domain.model.integration.PayPalDataCollectorConfig
import com.paydock.feature.paypal.fraud.injection.dataCollectorFailureTestModule
import com.paydock.feature.paypal.fraud.injection.dataCollectorSuccessTestModule
import com.paydock.initializeMobileSDK
import com.paydock.telemetry.Telemetry
import com.paypal.android.fraudprotection.PayPalDataCollectorRequest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import java.io.File
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class PayPalDataCollectorUtilTest : BaseUnitTest() {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Application
    private val config = PayPalDataCollectorConfig(
        accessToken = MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
        gatewayId = MobileSDKTestConstants.General.MOCK_GATEWAY_ID
    )

    @Before
    fun setup() {
        // Mock ProcessLifecycleOwner for BinDataRefreshCoordinator
        mockkObject(ProcessLifecycleOwner)
        val processLifecycleOwner = mockk<ProcessLifecycleOwner>(relaxed = true)
        val lifecycleRegistry = LifecycleRegistry(processLifecycleOwner)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        every { ProcessLifecycleOwner.get() } returns processLifecycleOwner
        every { processLifecycleOwner.lifecycle } returns lifecycleRegistry

        // Mock Application so Koin can resolve androidApplication() in presentationModule
        context = mockk<Application>(relaxed = true)
        every { context.applicationContext } returns context

        // Mock filesDir and cacheDir for BinDataCacheManager file operations
        val tempDir = File(System.getProperty("java.io.tmpdir"), "paydock_test")
        tempDir.mkdirs()
        every { context.filesDir } returns tempDir
        every { context.cacheDir } returns tempDir

        // Mock SharedPreferences for BinDataCacheManager
        val sharedPrefs = mockk<SharedPreferences>(relaxed = true)
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs

        val environment = Environment.SANDBOX
        context.initializeMobileSDK(environment)
    }

    @After
    fun resetMocks() {
        MobileSDK.reset() // Reset MobileSDK before each test
    }

    @After
    fun tearDownKoin() {
        // As the SDK will startKoin, we need to ensure that after each test we stop koin to be able to restart it in each test
        stopKoin()
    }

    @Test
    fun `create should return an initialized PayPalDataCollector class`() = runTest {
        unloadKoinModules(domainModule)
        loadKoinModules(dataCollectorSuccessTestModule)
        // Call create
        val result = PayPalDataCollectorUtil.initialise(config = config)
        // Assert that the result is not null and initialized
        assertNotNull(result)
    }

    @Test(expected = PayPalDataCollectorException.InitialisationClientIdException::class)
    fun `create should throw InitialisationClientIdException when the getPayPalClientIdUseCase fails`() = runTest {
        unloadKoinModules(domainModule)
        loadKoinModules(dataCollectorFailureTestModule)
        // Assert that an InitialisationClientIdException is thrown
        val result = PayPalDataCollectorUtil.initialise(config = config)
        // Assert that the result is null and not initialized
        assertNull(result)
    }

    @Test
    fun `initialise client id failure reports one SdkError and still throws InitialisationClientIdException (PD1)`() =
        runTest {
            unloadKoinModules(domainModule)
            loadKoinModules(dataCollectorFailureTestModule)
            val telemetry = RecordingTelemetry()
            loadKoinModules(module { single<Telemetry> { telemetry } })

            val thrown = assertFailsWith<PayPalDataCollectorException.InitialisationClientIdException> {
                PayPalDataCollectorUtil.initialise(config = config)
            }

            val reported = telemetry.sdkErrors.single()
            assertNull(reported.widget)
            assertEquals("InitialisationClientIdException", reported.errorCode)
            assertEquals(
                listOf(PayPalVaultException.GetPayPalClientIdException::class.java.name),
                reported.error.causeTypes.take(1)
            )
            assertEquals(thrown.error.status, reported.apiError?.status)
            assertEquals("gateway_client_id", reported.apiError?.summaryCode)
        }

    @Test
    fun `initialise other failure reports one SdkError and still throws UnknownException (PD2)`() = runTest {
        unloadKoinModules(domainModule)
        loadKoinModules(
            module {
                single {
                    mockk<GetPayPalClientIdUseCase>(relaxed = false) {
                        coEvery { this@mockk(any(), any()) } returns Result.failure<String>(IllegalStateException())
                    }
                }
            }
        )
        val telemetry = RecordingTelemetry()
        loadKoinModules(module { single<Telemetry> { telemetry } })

        val thrown = assertFailsWith<PayPalDataCollectorException.UnknownException> {
            PayPalDataCollectorUtil.initialise(config = config)
        }

        assertEquals(MobileSDKConstants.PayPalVaultConfig.Errors.DATA_COLLECTOR_UNKNOWN_ERROR, thrown.message)
        val reported = telemetry.sdkErrors.single()
        assertNull(reported.widget)
        assertEquals("UnknownException", reported.errorCode)
        assertEquals(listOf(IllegalStateException::class.java.name), reported.error.causeTypes.take(1))
    }

    @Test
    fun `collectDeviceInfo should return paypalClientMetaDataId when initialized`() {
        // Set the mockPayPalCollector to the payPalDataCollector instance
        val randomMetaDataId = UUID.randomUUID().toString()
        val mockPayPalCollector = mockk<PayPalDataCollectorUtil>(relaxed = true)
        every { mockPayPalCollector.collectDeviceId(any(), any(), any(), any()) } returns randomMetaDataId

        // Call collectDeviceInfo
        val result = mockPayPalCollector.collectDeviceId(context = mockk(), hasUserLocationConsent = true)

        // Assert that the result is not null and equals the expected value
        assertNotNull(result)
        assertEquals(randomMetaDataId, result)
    }

    @Test
    fun `collectDeviceInfo should pass correct parameters to PayPalDataCollectorRequest`() {
        // Mock the PayPalDataCollector instance
        val randomMetaDataId = UUID.randomUUID().toString()
        val mockPayPalCollector = mockk<PayPalDataCollectorUtil>(relaxed = true)

        // Call collectDeviceInfo with specific parameters
        val context = mockk<Context>()
        val hasUserLocationConsent = true
        val clientMetadataId = "testId"
        val additionalData = mapOf("key" to "value")

        // The slot needs to be declared outside the every block
        val slot = slot<PayPalDataCollectorRequest>()
        every {
            mockPayPalCollector.collectDeviceId(context, hasUserLocationConsent, clientMetadataId, additionalData)
        } answers {
            val request = PayPalDataCollectorRequest(
                hasUserLocationConsent = arg(1),
                clientMetadataId = arg(2),
                additionalData = arg(3)
            )
            slot.captured = request // Capture the request object
            randomMetaDataId
        }

        mockPayPalCollector.collectDeviceId(context, hasUserLocationConsent, clientMetadataId, additionalData)

        // Verify that PayPalDataCollectorRequest was called with the correct parameters
        verify { mockPayPalCollector.collectDeviceId(context, hasUserLocationConsent, clientMetadataId, additionalData) }
        val capturedRequest = slot.captured
        assertEquals(hasUserLocationConsent, capturedRequest.hasUserLocationConsent)
        assertEquals(clientMetadataId, capturedRequest.clientMetadataId)
        assertEquals(additionalData, capturedRequest.additionalData)
    }

}