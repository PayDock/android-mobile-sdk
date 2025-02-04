package com.paydock.feature.paypal.fraud.injection

import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.feature.paypal.core.domain.usecase.GetPayPalClientIdUseCase
import com.paydock.feature.paypal.vault.presentation.viewmodels.PayPalVaultViewModelTest.Companion.MOCK_CLIENT_ID
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
internal val dataCollectorSuccessTestModule = module {
    single {
        mockk<GetPayPalClientIdUseCase>(relaxed = false) {
            coEvery { this@mockk(any(), any()) } returns Result.success(MOCK_CLIENT_ID)
        }
    }
}

internal val dataCollectorFailureTestModule = module {
    single {
        val mockError = PayPalVaultException.GetPayPalClientIdException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "gateway_client_id",
                    message = MobileSDKTestConstants.Errors.MOCK_CLIENT_ID_ERROR
                )
            )
        )
        mockk<GetPayPalClientIdUseCase>(relaxed = false) {
            coEvery { this@mockk(any(), any()) } returns Result.failure<String>(mockError)
        }
    }
}