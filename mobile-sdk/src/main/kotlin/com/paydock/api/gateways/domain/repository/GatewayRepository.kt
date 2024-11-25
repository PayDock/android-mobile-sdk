package com.paydock.api.gateways.domain.repository

/**
 * Interface that defines the operations for interacting with payment gateways.
 *
 * The `GatewayRepository` provides methods for retrieving essential data required to interact with
 * payment gateways, such as fetching the client ID for a specific gateway. Implementations of this
 * interface will handle network calls to interact with backend services for gateway-related operations.
 */
internal interface GatewayRepository {

    /**
     * Retrieves the Payment Source client ID for a specific payment gateway.
     *
     * This function sends a request to fetch the client ID associated with the provided gateway
     * identifier. It uses the provided OAuth token for authentication. The client ID is required to
     * initiate gateway transactions.
     *
     * @param accessToken The OAuth token used for authenticating the request.
     * @param gatewayId The identifier for the payment gateway.
     * @return The client ID if available, or `null` if the client ID could not be retrieved.
     */
    suspend fun getGatewayClientId(accessToken: String, gatewayId: String): String
}
