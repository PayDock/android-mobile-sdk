package com.paydock.feature.card.domain.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.utils.reader.LocalFileReader
import com.paydock.feature.card.data.dto.CardSchemasResponse
import com.paydock.feature.card.data.mapper.asEntity
import com.paydock.feature.card.domain.repository.CardRepository
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
            readResourceFile("card/success_get_card_schemas_response.json")
        coEvery {
            fileReader.readFileFromAssets(any())
        } returns expectedResult

        getCardSchemasUseCase = GetCardSchemasUseCase(
            mockRepository
        )
    }

    @Test
    fun `test valid access token returns expected card schemas list result`() = runTest {
        // GIVEN
        val expectedResult =
            readResourceFile("card/success_get_card_schemas_response.json")
                .convertToDataClass<CardSchemasResponse>().asEntity()
        coEvery {
            mockRepository.getCardSchemas()
        } returns expectedResult
        // WHEN
        val actualResult = getCardSchemasUseCase()
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) {
            mockRepository.getCardSchemas()
        }
    }

    @Test
    fun `test invalid file throws exception result`() = runTest {
        // GIVEN
        val expectedResult = IllegalArgumentException("Unable to parse json file.")
        coEvery {
            mockRepository.getCardSchemas()
        } throws expectedResult
        // WHEN
        val actualResult =
            getCardSchemasUseCase()

        // THEN
        assertTrue(actualResult.isFailure)
        coVerify(exactly = 1) {
            mockRepository.getCardSchemas()
        }
    }

}