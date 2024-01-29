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

package com.paydock.feature.card.domain.repository

import com.paydock.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import kotlinx.coroutines.flow.Flow

/**
 * Represents a repository responsible for handling tokenization of card details.
 */
internal interface CardDetailsRepository {
    /**
     * Tokenizes the provided card details using the given [request].
     *
     * @param request The [TokeniseCardRequest] representing the card details to be tokenized.
     * @return A [TokenisedCardDetails] object containing the token and type of the tokenized card.
     */
    suspend fun tokeniseCardDetails(request: TokeniseCardRequest): TokenisedCardDetails

    /**
     * Tokenizes card details using a flow-based approach.
     *
     * This function takes a [request] containing card details and returns a [Flow] of [TokenisedCardDetails].
     * The function uses Kotlin coroutines and Flow to perform the tokenization asynchronously.
     *
     * @param request The [TokeniseCardRequest] containing card details to be tokenized.
     * @return A [Flow] of [TokenisedCardDetails] representing the tokenized card details.
     */
    fun tokeniseCardDetailsFlow(request: TokeniseCardRequest): Flow<TokenisedCardDetails>
}