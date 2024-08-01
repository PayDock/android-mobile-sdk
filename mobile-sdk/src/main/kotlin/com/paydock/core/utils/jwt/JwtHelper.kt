package com.paydock.core.utils.jwt

import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.utils.jwt.models.MetaTokenPayload
import com.paydock.core.utils.jwt.models.WalletTokenPayload
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

/**
 * Helper class for decoding and working with JWT tokens.
 */
internal object JwtHelper {

    // Cache to store decoded payloads
    private val payloadCache = ConcurrentHashMap<String, Any>()

    /**
     * Decodes the JWT token payload, caches it, and converts it to the specified type.
     * @param jwt The JWT token to decode.
     * @return The decoded payload as the specified type, or null if decoding fails.
     */
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    private inline fun <reified T : Any> decodeAndCachePayload(jwt: String): T? {
        return try {
            val payload = decodeTokenPayload(jwt)
            if (payload != null) {
                payloadCache[jwt] = payload
            }
            payload?.convertToDataClass<T>()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decodes the JWT token payload.
     * @param jwt The JWT token to decode.
     * @return The decoded payload as a String, or null if decoding fails.
     */
    @Suppress("MagicNumber")
    private fun decodeTokenPayload(jwt: String): String? {
        val parts = jwt.split(".")
        return when (parts.size) {
            3 -> decodeBase64(parts[1])
            1 -> decodeBase64(jwt)
            else -> null
        }
    }

    /**
     * Decodes a Base64-encoded string.
     * @param input The Base64-encoded string to decode.
     * @return The decoded string.
     */
    private fun decodeBase64(input: String): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            java.util.Base64.getUrlDecoder().decode(input).toString(Charsets.UTF_8)
        } else {
            return android.util.Base64.decode(input, android.util.Base64.URL_SAFE)
                .toString(Charsets.UTF_8)
        }
    }

    /**
     * Gets the decoded payload from the cache or decodes it if not cached.
     * @param jwt The JWT token.
     * @return The decoded payload as the specified type, or null if decoding fails.
     */
    private inline fun <reified T : Any> getDecodedPayload(jwt: String): T? {
        return payloadCache[jwt] as? T ?: decodeAndCachePayload(jwt)
    }

    /**
     * Gets the decoded payload and casts it to the specified type.
     * @param jwt The JWT token.
     * @return The decoded payload as the specified type, or null if decoding fails.
     */
    private inline fun <reified T : Any> getPayloadOfType(jwt: String): T? =
        getDecodedPayload<T>(jwt)

    /**
     * Retrieves the WalletTokenPayload from the JWT token.
     * @param walletToken The JWT token.
     * @return The WalletTokenPayload, or null if decoding fails.
     */
    fun getWalletTokenPayload(walletToken: String): WalletTokenPayload? =
        getPayloadOfType<WalletTokenPayload>(walletToken)

    /**
     * Retrieves the MetaTokenPayload from the JWT token.
     * @param walletToken The JWT token.
     * @return The MetaTokenPayload, or null if decoding fails.
     */
    fun getMetaTokenPayload(walletToken: String): MetaTokenPayload? =
        getPayloadOfType<WalletTokenPayload>(walletToken)?.meta?.let { metaToken ->
            getPayloadOfType<MetaTokenPayload>(
                metaToken
            )
        }

    /**
     * Retrieves the charge ID from the JWT token.
     * @param walletToken The JWT token.
     * @return The charge ID, or null if not found or decoding fails.
     */
    fun getChargeIdToken(walletToken: String): String? =
        getMetaTokenPayload(walletToken)?.meta?.charge?.id

    /**
     * Retrieves the token expiration date from the JWT token.
     * @param walletToken The JWT token.
     * @return The expiration date, or null if not found or decoding fails.
     */
    fun getTokenExpirationDate(walletToken: String): Date? {
        val decodedPayload = getPayloadOfType<WalletTokenPayload>(walletToken)
        val expTimestamp = decodedPayload?.exp ?: return null
        return Date(expTimestamp.toLong() * 1000)
    }

    /**
     * Checks if the token is expired.
     * @param walletToken The JWT token.
     * @return True if the token is expired, false otherwise.
     */
    fun isTokenExpired(walletToken: String): Boolean {
        val expirationDate = getTokenExpirationDate(walletToken) ?: return true
        val currentTime = Date()
        return currentTime.after(expirationDate)
    }
}