package com.paydock.api.management.data.repository

import com.paydock.api.management.data.dto.CardSchemasResponse
import com.paydock.api.management.data.mapper.asEntity
import com.paydock.api.management.domain.model.CardSchema
import com.paydock.api.management.domain.repository.ManagementRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.path
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.TreeMap

/**
 * Implementation of [ManagementRepository] for managing card-related operations.
 *
 * @param dispatcher The coroutine dispatcher for executing operations.
 * @param client The HTTP client used for making network requests.
 */
internal class ManagementRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val client: HttpClient
) : ManagementRepository {

    /**
     * Fetches the supported card schemas from the backend.
     *
     * @param token The access token required for authentication.
     * @return A [TreeMap] containing card schema information.
     */
    override suspend fun getCardSchemas(token: String): TreeMap<Int, CardSchema> =
        withContext(dispatcher) {
            val httpResponse: HttpResponse = client.get {
                headers { append("x-access-token", token) }
                url { path("/v1/bin-management/card_schemas") }
            }
            httpResponse.body<CardSchemasResponse>().asEntity()
        }
}