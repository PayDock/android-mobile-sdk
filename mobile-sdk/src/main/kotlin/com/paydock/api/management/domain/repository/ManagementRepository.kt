package com.paydock.api.management.domain.repository

import com.paydock.api.management.domain.model.CardSchema
import java.util.TreeMap

/**
 * Repository interface for managing card-related operations.
 */
internal interface ManagementRepository {

    /**
     * Fetches the supported card schemas from the backend.
     *
     * @param token The access token required for authentication.
     * @return A [TreeMap] containing card schema information.
     */
    suspend fun getCardSchemas(token: String): TreeMap<Int, CardSchema>
}