package com.paydock.sample.feature.wallet.data.repository

import com.paydock.sample.feature.wallet.data.api.WalletApi
import com.paydock.sample.feature.wallet.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.wallet.data.mapper.toDomain
import com.paydock.sample.feature.wallet.data.model.WalletCharge
import com.paydock.sample.feature.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    private val walletApi: WalletApi,
) : WalletRepository {

    override suspend fun initiateWalletTransaction(
        manualCapture: Boolean,
        request: InitiateWalletRequest,
    ): WalletCharge =
        withContext(dispatcher) {
            if (manualCapture) {
                walletApi.initiateWalletTransactionManualCapture(request = request).toDomain()
            } else {
                walletApi.initiateWalletTransaction(request = request).toDomain()

            }
        }

    override suspend fun captureWalletCharge(chargeId: String): WalletCharge =
        withContext(dispatcher) {
            walletApi.captureWalletCharge(id = chargeId).toDomain()
        }
}