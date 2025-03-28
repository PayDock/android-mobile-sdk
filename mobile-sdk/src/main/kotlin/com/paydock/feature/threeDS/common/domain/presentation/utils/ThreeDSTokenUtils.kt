package com.paydock.feature.threeDS.common.domain.presentation.utils

import android.util.Base64
import com.paydock.core.extensions.multiCatch
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.threeDS.common.domain.model.ui.Token
import kotlinx.serialization.SerializationException

/**
 * Utility class for handling 3DS Token-related operations.
 */
internal object ThreeDSTokenUtils {

    /**
     * Extracts a Token object from a base64 encoded string.
     *
     * This function takes a base64 encoded string, decodes it, and then
     * attempts to parse the resulting JSON string into a `Token` data class.
     * If any errors occur during the decoding or parsing process, it returns null.
     *
     * @param encodedToken The base64 encoded string representing the token data.
     * @return A `Token` object if the decoding and parsing are successful, or null otherwise.
     *
     * @throws IllegalArgumentException if the input `encodedToken` is not a valid base64 string or
     *      if the decoded content isn't a valid JSON.
     * @throws SerializationException if the JSON string cannot be parsed into a `Token` object.
     */
    fun extractToken(encodedToken: String): Token? {
        return {
            // Decode the base64 token
            val decodedJson = String(Base64.decode(encodedToken, Base64.DEFAULT))
            // Parse the JSON using Gson
            decodedJson.convertToDataClass<Token>()
        }.multiCatch(IllegalArgumentException::class, SerializationException::class) {
            null
        }
    }
}