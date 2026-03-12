package com.paydock.feature.card.domain.usecase

import com.paydock.binprocessor.data.dto.BinDataResponse
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.network.extensions.convertToDataClass
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

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()

        getCardSchemasUseCase = GetCardSchemasUseCase(
            mockRepository
        )
    }

    @Test
    fun `test valid access token returns expected BIN data result`() = runTest {
        // GIVEN
        val expectedResult =
            readResourceFile("card/success_bin_data_response.json")
                .convertToDataClass<BinDataResponse>()
        coEvery {
            mockRepository.getBinData()
        } returns expectedResult
        // WHEN
        val actualResult = getCardSchemasUseCase()
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult.version, actualResult.getOrNull()?.version)
        assertEquals(expectedResult.schemes, actualResult.getOrNull()?.schemes)
        coVerify(exactly = 1) {
            mockRepository.getBinData()
        }
    }

    @Test
    fun `test invalid file throws exception result`() = runTest {
        // GIVEN
        val expectedResult = IllegalArgumentException("Unable to parse json file.")
        coEvery {
            mockRepository.getBinData()
        } throws expectedResult
        // WHEN
        val actualResult =
            getCardSchemasUseCase()

        // THEN
        assertTrue(actualResult.isFailure)
        coVerify(exactly = 1) {
            mockRepository.getBinData()
        }
    }

}