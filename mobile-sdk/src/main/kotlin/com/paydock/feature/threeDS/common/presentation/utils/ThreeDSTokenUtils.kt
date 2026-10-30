package com.paydock.feature.threeDS.common.presentation.utils

import android.util.Base64
import com.paydock.core.extensions.multiCatch
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.threeDS.common.domain.model.ui.Token
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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

    /**
     * Extracts the 3DS charge ID from a base64 encoded 3DS token.
     *
     * MPGS tokens carry it in the token itself. Standalone 3DS tokens carry it inside the `meta` claim of the JWT
     * in `content`, which is where this falls back to.
     *
     * @param encodedToken The base64 encoded string representing the token data.
     * @return The 3DS charge ID, or null if the token can't be decoded or doesn't carry one.
     */
    fun extractCharge3dsId(encodedToken: String): String? {
        val token = extractToken(encodedToken) ?: return null
        return token.charge3dsId ?: runCatching {
            val claims = Json.parseToJsonElement(token.content.split('.')[1].decodeBase64Url()).jsonObject
            val meta = Json.parseToJsonElement(claims.getValue("meta").jsonPrimitive.content.decodeBase64Url())
            meta.jsonObject["charge_3ds_id"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()
    }

    /**
     * Decodes base64 or base64url text (JWT parts use base64url without padding).
     */
    private fun String.decodeBase64Url(): String =
        String(Base64.decode(replace('-', '+').replace('_', '/'), Base64.DEFAULT))
}