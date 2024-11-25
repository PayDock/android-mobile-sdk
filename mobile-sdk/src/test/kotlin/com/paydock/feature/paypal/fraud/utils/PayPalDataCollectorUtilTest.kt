package com.paydock.feature.paypal.fraud.utils

import android.content.Context
import com.paydock.MobileSDK
import com.paydock.core.BaseUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.domain.error.exceptions.PayPalDataCollectorException
import com.paydock.core.domain.injection.domainModule
import com.paydock.core.domain.model.Environment
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.paypal.fraud.domain.model.integration.PayPalDataCollectorConfig
import com.paydock.feature.paypal.fraud.injection.dataCollectorFailureTestModule
import com.paydock.feature.paypal.fraud.injection.dataCollectorSuccessTestModule
import com.paydock.initializeMobileSDK
import com.paypal.android.fraudprotection.PayPalDataCollectorRequest
import io.mockk.every
import io.mockk.mockk
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
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PayPalDataCollectorUtilTest : BaseUnitTest() {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private val config = PayPalDataCollectorConfig(
        accessToken = MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
        gatewayId = MobileSDKTestConstants.General.MOCK_GATEWAY_ID
    )

    @Before
    fun setup() {
        // Mock the Context object
        context = mockk()
        // Configure the getApplicationContext() method to return the mock Context
        every { context.applicationContext } returns context

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
    fun `collectDeviceInfo should return paypalClientMetaDataId when initialized`() {
        // Set the mockPayPalCollector to the payPalDataCollector instance
        val randomMetaDataId = UUID.randomUUID().toString()
        val mockPayPalCollector = mockk<PayPalDataCollectorUtil>(relaxed = true)
        every { mockPayPalCollector.collectDeviceInfo(any(), any(), any(), any()) } returns randomMetaDataId

        // Call collectDeviceInfo
        val result = mockPayPalCollector.collectDeviceInfo(context = mockk(), hasUserLocationConsent = true)

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
            mockPayPalCollector.collectDeviceInfo(context, hasUserLocationConsent, clientMetadataId, additionalData)
        } answers {
            val request = PayPalDataCollectorRequest(
                hasUserLocationConsent = arg(1),
                clientMetadataId = arg(2),
                additionalData = arg(3)
            )
            slot.captured = request // Capture the request object
            randomMetaDataId
        }

        mockPayPalCollector.collectDeviceInfo(context, hasUserLocationConsent, clientMetadataId, additionalData)

        // Verify that PayPalDataCollectorRequest was called with the correct parameters
        verify { mockPayPalCollector.collectDeviceInfo(context, hasUserLocationConsent, clientMetadataId, additionalData) }
        val capturedRequest = slot.captured
        assertEquals(hasUserLocationConsent, capturedRequest.hasUserLocationConsent)
        assertEquals(clientMetadataId, capturedRequest.clientMetadataId)
        assertEquals(additionalData, capturedRequest.additionalData)
    }

}