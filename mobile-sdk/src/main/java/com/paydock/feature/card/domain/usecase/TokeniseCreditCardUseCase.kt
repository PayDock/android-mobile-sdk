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

package com.paydock.feature.card.domain.usecase

import com.paydock.core.extensions.suspendRunCatching
import com.paydock.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import com.paydock.feature.card.domain.repository.CardDetailsRepository

/**
 * Use case responsible for tokenizing credit card details.
 *
 * This use case interacts with the provided [CardDetailsRepository] to request tokenization
 * of credit card information based on the given [TokeniseCardRequest.CreditCard].
 *
 * @param repository The repository that handles the tokenization request.
 */
internal class TokeniseCreditCardUseCase(private val repository: CardDetailsRepository) {
    /**
     * Invokes the use case to tokenize credit card details.
     *
     * @param request The [TokeniseCardRequest.CreditCard] containing credit card information to tokenize.
     * @return A [Result] representing the tokenization result.
     */
    suspend operator fun invoke(request: TokeniseCardRequest.CreditCard): Result<TokenisedCardDetails> = suspendRunCatching {
        repository.tokeniseCardDetails(request)
    }
}
