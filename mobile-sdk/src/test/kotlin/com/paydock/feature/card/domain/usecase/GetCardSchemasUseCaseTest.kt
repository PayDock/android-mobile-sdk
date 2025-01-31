package com.paydock.feature.card.domain.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.utils.reader.LocalFileReader
import com.paydock.feature.card.data.dto.CardSchemasResponse
import com.paydock.feature.card.data.mapper.asEntity
import com.paydock.feature.card.domain.repository.CardRepository
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GetCardSchemasUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: CardRepository
    private lateinit var getCardSchemasUseCase: GetCardSchemasUseCase
    private lateinit var fileReader: LocalFileReader

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        fileReader = mockk()

        val expectedResult =
            readResourceFile("management/success_get_card_schemas_response.json")
        coEvery {
            fileReader.readFileFromAssets(any())
        } returns expectedResult

        getCardSchemasUseCase = GetCardSchemasUseCase(
            mockRepository,
            fileReader
        )
    }

    @Test
    fun `test valid access token returns expected card schemas list result`() = runTest {
        // GIVEN
        val expectedResult =
            readResourceFile("management/success_get_card_schemas_response.json")
                .convertToDataClass<CardSchemasResponse>().asEntity()
        coEvery {
            mockRepository.getCardSchemas(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN
            )
        } returns expectedResult
        // WHEN
        val actualResult =
            getCardSchemasUseCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) {
            mockRepository.getCardSchemas(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN
            )
        }
    }

    @Test
    fun `test invalid access token returns expected error result`() = runTest {
        // GIVEN
        val expectedResult =
            ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    summary = ErrorSummary(
                        code = "bad_request",
                        message = MobileSDKTestConstants.Errors.MOCK_AUTH_TOKEN_ERROR
                    )
                )
            )
        coEvery {
            mockRepository.getCardSchemas(
                MobileSDKTestConstants.General.MOCK_INVALID_ACCESS_TOKEN
            )
        } throws expectedResult
        val fallbackResult =
            readResourceFile("management/success_get_card_schemas_response.json")
                .convertToDataClass<CardSchemasResponse>().asEntity()
        // WHEN
        val actualResult =
            getCardSchemasUseCase(
                MobileSDKTestConstants.General.MOCK_INVALID_ACCESS_TOKEN
            )

        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(fallbackResult, actualResult.getOrNull())
        coVerify(exactly = 1) {
            mockRepository.getCardSchemas(
                MobileSDKTestConstants.General.MOCK_INVALID_ACCESS_TOKEN
            )
        }
    }

}