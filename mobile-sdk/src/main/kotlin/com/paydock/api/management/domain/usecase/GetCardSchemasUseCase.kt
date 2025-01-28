package com.paydock.api.management.domain.usecase

import com.paydock.api.management.data.dto.CardSchemasResponse
import com.paydock.api.management.data.mapper.asEntity
import com.paydock.api.management.domain.model.CardSchema
import com.paydock.api.management.domain.repository.ManagementRepository
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.domain.error.extensions.mapCardDetailsApiException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.utils.reader.LocalFileReader
import java.util.TreeMap

/**
 * Use case for retrieving the supported card schemas from the repository.
 * This use case first attempts to fetch card schemas from the remote repository.
 * If the remote fetch fails due to a network error or an I/O exception,
 * it falls back to loading card schemas from a local JSON file.
 *
 * @param repository The repository responsible for fetching card schemas from the remote source.
 * @param jsonReader A reader for accessing local JSON files.
 */
internal class GetCardSchemasUseCase(
    private val repository: ManagementRepository,
    private val jsonReader: LocalFileReader
) {

    /**
     * Executes the use case to retrieve card schemas.
     *
     * @param accessToken The access token required for authenticating with the remote repository.
     * @return A `Result` object containing either a `TreeMap` of card schemas if successful,
     * or a `CardDetailsException.GetCardSchemasException` if an error occurred.
     */
    suspend operator fun invoke(accessToken: String): Result<TreeMap<Int, CardSchema>> = try {
        Result.success(repository.getCardSchemas(accessToken))
    } catch (@Suppress("TooGenericExceptionCaught") exception: Exception) {
        handleGetCardSchemasError(exception)
    }

    /**
     * Handles errors that occur during the retrieval of card schemas.
     * This function attempts to load card schemas from a local JSON file as a fallback.
     *
     * @param exception The exception that triggered the error handling.
     * @return A `Result` object containing either a `TreeMap` of card schemas if the fallback is successful,
     * or a `CardDetailsException.GetCardSchemasException` if the fallback also fails.
     */
    private fun handleGetCardSchemasError(exception: Exception): Result<TreeMap<Int, CardSchema>> {
        return try {
            val jsonString = jsonReader.readFileFromAssets("card_schemas.json")
            val response = jsonString.convertToDataClass<CardSchemasResponse>()
            Result.success(response.asEntity())
        } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") e: Exception) {
            Result.failure(exception.mapCardDetailsApiException<CardDetailsException.GetCardSchemasException>())
        }
    }
}