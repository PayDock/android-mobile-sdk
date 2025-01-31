package com.paydock.feature.card.data.repository

import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.utils.reader.LocalFileReader
import com.paydock.feature.card.data.dto.CardSchemasResponse
import com.paydock.feature.card.data.dto.CardTokenResponse
import com.paydock.feature.card.data.dto.CreateCardPaymentTokenRequest
import com.paydock.feature.card.data.mapper.asEntity
import com.paydock.feature.card.domain.model.ui.CardSchema
import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.card.domain.repository.CardRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.path
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.TreeMap

/**
 * Implementation of the [CardRepository] interface for handling payment token operations
 * related to payment gateways, specifically for the PayPal API.
 *
 * This class provides methods to create payment tokens, session authentication tokens,
 * and setup tokens by performing HTTP requests to the PayPal API.
 *
 * @param dispatcher The CoroutineDispatcher used for executing network calls.
 * @param client The [HttpClient] instance used to make HTTP requests.
 * @param jsonReader A reader for accessing local JSON files.
 */
internal class CardRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val client: HttpClient,
    private val jsonReader: LocalFileReader
) : CardRepository {

    /**
     * Creates a payment token using the provided access token and request data.
     *
     * This method performs a POST request to the Paydock API to generate a payment token based on
     * the provided [CreateCardPaymentTokenRequest]. It runs in the specified [dispatcher] context.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreateCardPaymentTokenRequest] containing the necessary parameters for token creation.
     * @return A [TokenDetails] object containing the generated payment token and its associated type.
     */
    override suspend fun createPaymentToken(
        accessToken: String,
        request: CreateCardPaymentTokenRequest
    ): TokenDetails = withContext(dispatcher) {
        val httpResponse: HttpResponse = client.post {
            headers { append("x-access-token", accessToken) }
            url { path("/v1/payment_sources/tokens") }
            setBody(request)
        }
        httpResponse.body<CardTokenResponse>().asEntity()
    }

    /**
     * Creates a flow for generating a payment token asynchronously.
     *
     * This function performs a POST request to the PayPal API and emits the resulting
     * [TokenDetails] as a flow. This can be useful for observing changes or handling
     * updates in the token creation process.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreateCardPaymentTokenRequest] containing the necessary parameters for token creation.
     * @return A [Flow] emitting [TokenDetails] as the payment token is created.
     */
    override fun createPaymentTokenFlow(
        accessToken: String,
        request: CreateCardPaymentTokenRequest
    ): Flow<TokenDetails> = flow {
        emit(
            client.post {
                headers { append("x-access-token", accessToken) }
                url { path("/v1/payment_sources/tokens") }
                setBody(request)
            }.body<CardTokenResponse>().asEntity()
        )
    }.flowOn(dispatcher)

    /**
     * Fetches the supported card schemas from the a local file.
     *
     * @return A [TreeMap] containing card schema information.
     */
    override suspend fun getCardSchemas(): TreeMap<Int, CardSchema> =
        withContext(dispatcher) {
            val jsonString = jsonReader.readFileFromAssets("card_schemas.json")
            val response = jsonString.convertToDataClass<CardSchemasResponse>()
            response.asEntity()
        }
}