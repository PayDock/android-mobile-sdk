package com.paydock.feature.threeDS.common.domain.model.ui.enums

import kotlinx.serialization.SerialName

/**
 * Represents the different formats in which a token can be presented.
 *
 * This enum defines the possible formats for a token, which can be:
 * - [HTML]: The token is embedded within an HTML structure.
 * - [URL]: The token is a URL pointing to the relevant resource.
 * - [STANDALONE_3DS]: The token is specifically for standalone 3D Secure flows.
 */
internal enum class TokenFormat {
    @SerialName("html")
    HTML,

    @SerialName("url")
    URL,

    @SerialName("standalone_3ds")
    STANDALONE_3DS
}