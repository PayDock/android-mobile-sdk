package com.paydock.feature.card.domain.usecase

import com.paydock.core.extensions.suspendRunCatching
import com.paydock.feature.card.domain.model.ui.CardSchema
import com.paydock.feature.card.domain.repository.CardRepository
import java.util.TreeMap

/**
 * Use case for retrieving the supported card schemas from the repository.
 *
 * @param repository The repository responsible for fetching card schemas from the remote source.
 */
internal class GetCardSchemasUseCase(private val repository: CardRepository) {

    /**
     * Invoke the use case to fetch list of local card schemas.
     *
     * @return A `Result` object containing either a `TreeMap` of card schemas if successful,
     */
    suspend operator fun invoke(): Result<TreeMap<Int, CardSchema>> = suspendRunCatching {
        repository.getCardSchemas()
    }
}