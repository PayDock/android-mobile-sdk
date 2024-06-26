package com.paydock.core.utils.decoder

/**
 * Interface for decoding encoded strings.
 */
interface StringDecoder {
    /**
     * Decodes an encoded string and returns the decoded result.
     *
     * @param encodedString The string to be decoded.
     * @return The decoded string.
     */
    fun decodeString(encodedString: String): String
}
