package com.paydock.feature.threeDS.common.domain.integration

/**
 * Represents the configuration for a 3D Secure process.
 *
 * This data class holds the necessary information to initiate and manage a 3D Secure authentication flow.
 *
 * @property token The unique token associated with the 3D Secure transaction.
 */
data class ThreeDSConfig(
    val token: String
)
