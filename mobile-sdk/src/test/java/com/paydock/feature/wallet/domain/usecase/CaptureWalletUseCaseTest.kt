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
import com.paydock.feature.wallet.data.api.dto.WalletCaptureRequest
import com.paydock.feature.wallet.data.api.dto.WalletCaptureResponse
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

class CaptureWalletUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: WalletRepository
    private lateinit var useCase: CaptureWalletTransactionUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        useCase = CaptureWalletTransactionUseCase(mockRepository)
    }

    @Test
    fun `test valid PayPal capture wallet request returns expected charge response`() = runTest {
        // GIVEN
        val validAccessToken = "valid-token"
        val request =
            readResourceFile("wallet/valid_paypal_capture_wallet_charge_request.json").convertToDataClass<WalletCaptureRequest>()
        val response =
            readResourceFile("wallet/success_capture_wallet_response.json").convertToDataClass<WalletCaptureResponse>()
        val expectedResult = response.asEntity()
        coEvery { mockRepository.captureWalletTransaction(validAccessToken, request) } returns expectedResult
        // WHEN
        val actualResult = useCase(validAccessToken, request)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) { mockRepository.captureWalletTransaction(validAccessToken, request) }
    }

    @Test
    fun `test valid GooglePay capture wallet request returns expected charge response`() = runTest {
        // GIVEN
        val validAccessToken = "valid-token"
        val request =
            readResourceFile("wallet/valid_googlepay_capture_wallet_charge_request.json").convertToDataClass<WalletCaptureRequest>()
        val response =
            readResourceFile("wallet/success_capture_wallet_response.json").convertToDataClass<WalletCaptureResponse>()
        val expectedResult = response.asEntity()
        coEvery { mockRepository.captureWalletTransaction(validAccessToken, request) } returns expectedResult
        // WHEN
        val actualResult = useCase(validAccessToken, request)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) { mockRepository.captureWalletTransaction(validAccessToken, request) }
    }

    @Test
    fun `test invalid access token returns expected error resource`() = runTest {
        // GIVEN
        val invalidAccessToken = "invalid-token"

        val request =
            readResourceFile("wallet/valid_paypal_capture_wallet_charge_request.json").convertToDataClass<WalletCaptureRequest>()
        val expectedResult =
            ApiException(
                code = HttpStatusCode.InternalServerError.value,
                displayableMessage = "Unexpected error during process"
            )
        coEvery { mockRepository.captureWalletTransaction(invalidAccessToken, request) } throws expectedResult
        // WHEN
        val actualResult = useCase(invalidAccessToken, request)
        // THEN
        assertTrue(actualResult.isFailure)
        assertEquals(expectedResult, actualResult.exceptionOrNull())
        coVerify(exactly = 1) { mockRepository.captureWalletTransaction(invalidAccessToken, request) }
    }

}