package com.paydock.feature.zip.domain.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.domain.error.exceptions.ZipException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.feature.zip.domain.repository.ZipRepository
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.math.BigDecimal
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class InitializeZipCheckoutUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: ZipRepository
    private lateinit var initializeZipCheckoutUseCase: InitializeZipCheckoutUseCase

    private val testConfig = ZipWidgetConfig(
        accessToken = MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
        gatewayId = MobileSDKTestConstants.General.MOCK_GATEWAY_ID,
        amount = BigDecimal("100.00"),
        currency = "AUD",
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com"
    )

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        initializeZipCheckoutUseCase = InitializeZipCheckoutUseCase(mockRepository)
    }

    @Test
    fun `test valid initialize checkout request returns expected checkout data`() = runTest {
        // GIVEN
        val expectedCheckoutUrl = MobileSDKTestConstants.Zip.MOCK_CHECKOUT_URL
        val expectedCheckoutToken = MobileSDKTestConstants.Zip.MOCK_CHECKOUT_TOKEN
        val expectedResult = Pair(expectedCheckoutUrl, expectedCheckoutToken)

        coEvery {
            mockRepository.initializeExternalCheckout(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                testConfig
            )
        } returns expectedResult

        // WHEN
        val actualResult = initializeZipCheckoutUseCase(
            MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            testConfig
        )

        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) {
            mockRepository.initializeExternalCheckout(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                testConfig
            )
        }
    }

    @Test
    fun `test initialize checkout with invalid gateway returns expected error`() = runTest {
        // GIVEN
        val expectedException = ApiException(
            error = ApiErrorResponse(
                status = HttpStatusCode.BadRequest.value,
                summary = ErrorSummary(
                    code = "invalid_gateway",
                    message = MobileSDKTestConstants.Errors.MOCK_INVALID_GATEWAY_ID_ERROR
                )
            )
        )

        coEvery {
            mockRepository.initializeExternalCheckout(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                testConfig
            )
        } throws expectedException

        // WHEN
        val actualResult = initializeZipCheckoutUseCase(
            MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            testConfig
        )

        // THEN
        assertTrue(actualResult.isFailure)
        assertIs<ZipException.FetchingCheckoutUrlException>(actualResult.exceptionOrNull())
        coVerify(exactly = 1) {
            mockRepository.initializeExternalCheckout(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                testConfig
            )
        }
    }

    @Test
    fun `test initialize checkout with server error returns expected error`() = runTest {
        // GIVEN
        val expectedException = ApiException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "server_error",
                    message = MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR
                )
            )
        )

        coEvery {
            mockRepository.initializeExternalCheckout(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                testConfig
            )
        } throws expectedException

        // WHEN
        val actualResult = initializeZipCheckoutUseCase(
            MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            testConfig
        )

        // THEN
        assertTrue(actualResult.isFailure)
        assertIs<ZipException.FetchingCheckoutUrlException>(actualResult.exceptionOrNull())
    }
}
