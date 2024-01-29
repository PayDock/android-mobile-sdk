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

package com.paydock.feature.wallet.domain.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.network.error.exceptions.ApiException
import com.paydock.core.extensions.convertToDataClass
import com.paydock.feature.wallet.data.api.dto.WalletCallbackRequest
import com.paydock.feature.wallet.data.api.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.mapper.asEntity
import com.paydock.feature.wallet.domain.repository.WalletRepository
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetWalletUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: WalletRepository
    private lateinit var useCase: GetWalletCallbackUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        useCase = GetWalletCallbackUseCase(mockRepository)
    }

    @Test
    fun `test valid PayPal wallet callback request returns expected charge response`() = runTest {
        // GIVEN
        val validAccessToken = "valid-token"
        val request =
            readResourceFile("wallet/valid_paypal_wallet_callback_request.json").convertToDataClass<WalletCallbackRequest>()
        val response =
            readResourceFile("wallet/success_paypal_wallet_callback_response.json").convertToDataClass<WalletCallbackResponse>()
        val expectedResult = response.asEntity()
        coEvery { mockRepository.getWalletCallback(validAccessToken, request) } returns expectedResult
        // WHEN
        val actualResult = useCase(validAccessToken, request)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) { mockRepository.getWalletCallback(validAccessToken, request) }
    }

    @Test
    fun `test valid FlyPay wallet callback request returns expected charge response`() = runTest {
        // GIVEN
        val validAccessToken = "valid-token"
        val request =
            readResourceFile("wallet/valid_flypay_wallet_callback_request.json").convertToDataClass<WalletCallbackRequest>()
        val response =
            readResourceFile("wallet/success_flypay_wallet_callback_response.json").convertToDataClass<WalletCallbackResponse>()
        val expectedResult = response.asEntity()
        coEvery { mockRepository.getWalletCallback(validAccessToken, request) } returns expectedResult
        // WHEN
        val actualResult = useCase(validAccessToken, request)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) { mockRepository.getWalletCallback(validAccessToken, request) }
    }

    @Test
    fun `test invalid wallet type request returns expected error resource`() = runTest {
        // GIVEN
        val validAccessToken = "valid-token"

        val request =
            readResourceFile("wallet/invalid_wallet_callback_request.json").convertToDataClass<WalletCallbackRequest>()
        val expectedResult =
            ApiException(
                code = HttpStatusCode.BadRequest.value,
                displayableMessage = "wallet_type must be a valid enum value"
            )
        coEvery { mockRepository.getWalletCallback(validAccessToken, request) } throws expectedResult
        // WHEN
        val actualResult = useCase(validAccessToken, request)
        // THEN
        assertTrue(actualResult.isFailure)
        assertEquals(expectedResult, actualResult.exceptionOrNull())
        coVerify(exactly = 1) { mockRepository.getWalletCallback(validAccessToken, request) }
    }

}