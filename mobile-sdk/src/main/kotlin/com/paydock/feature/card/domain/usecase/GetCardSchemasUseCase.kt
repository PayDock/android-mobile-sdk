package com.paydock.feature.card.domain.usecase

import com.paydock.binprocessor.data.dto.BinDataResponse
import com.paydock.core.extensions.suspendRunCatching
import com.paydock.feature.card.domain.repository.CardRepository

/**
 * Use case for retrieving the BIN data from the repository.
 *
 * @param repository The repository responsible for fetching BIN data from cache or asset.
 */
internal class GetCardSchemasUseCase(private val repository: CardRepository) {

    /**
     * Invoke the use case to fetch BIN data.
     *
     * @return A `Result` object containing either a [BinDataResponse] if successful,
     * or an error if the operation fails.
     */
    suspend operator fun invoke(): Result<BinDataResponse> = suspendRunCatching {
        repository.getBinData()
    }
}