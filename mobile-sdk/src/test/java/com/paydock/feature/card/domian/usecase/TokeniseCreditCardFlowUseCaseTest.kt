/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.feature.card.domian.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.network.error.exceptions.ApiException
import com.paydock.core.extensions.convertToDataClass
import com.paydock.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import com.paydock.feature.card.domain.repository.CardDetailsRepository
import com.paydock.feature.card.domain.usecase.TokeniseCreditCardFlowUseCase
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokeniseCreditCardFlowUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: CardDetailsRepository
    private lateinit var tokeniseCreditCardUseCase: TokeniseCreditCardFlowUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        tokeniseCreditCardUseCase = TokeniseCreditCardFlowUseCase(mockRepository)
    }

    @Test
    fun `test valid tokenise credit card request returns expected card resource`() = runTest {
        // GIVEN
        val request =
            readResourceFile("card/valid_tokenise_credit_card_request.json").convertToDataClass<TokeniseCardRequest.CreditCard>()
        val expectedResult =
            TokenisedCardDetails(type = "token", token = "f6301700-dcfe-4640-aabf-eff4ee3d96a6")
        coEvery { mockRepository.tokeniseCardDetailsFlow(request) } returns flowOf(expectedResult)
        // WHEN
        val actualFlowResult = tokeniseCreditCardUseCase(request)
        // THEN
        // Assert the result is success and contains the expected data
        actualFlowResult.collect { result ->
            assertTrue(result.isSuccess)
            assertEquals(expectedResult, result.getOrNull())
            coVerify(exactly = 1) { mockRepository.tokeniseCardDetailsFlow(request) }
        }
    }

    @Test
    fun `test invalid tokenise credit card request returns expected error resource`() = runTest {
        // GIVEN
        val request =
            readResourceFile("card/invalid_tokenise_credit_card_request.json").convertToDataClass<TokeniseCardRequest.CreditCard>()
        val expectedResult =
            ApiException(
                code = HttpStatusCode.BadRequest.value,
                displayableMessage = "Invalid Transaction Details"
            )
        coEvery { mockRepository.tokeniseCardDetailsFlow(request) } returns flow { throw expectedResult }
        // WHEN
        val actualFlowResult = tokeniseCreditCardUseCase(request)
        // THEN
        // Assert the result is success and contains the expected data
        actualFlowResult.collect { result ->
            assertTrue(result.isFailure)
            assertEquals(expectedResult, result.exceptionOrNull())
            coVerify(exactly = 1) { mockRepository.tokeniseCardDetailsFlow(request) }
        }
    }

}