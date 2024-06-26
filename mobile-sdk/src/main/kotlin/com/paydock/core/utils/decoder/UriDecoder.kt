package com.paydock.core.utils.decoder

import android.net.Uri

/**
 * Implementation of the StringDecoder interface that uses Android's Uri class to decode strings.
 */
class UriDecoder : StringDecoder {

    /**
     * Decodes an encoded string using Android's Uri class.
     *
     * @param encodedString The string to be decoded.
     * @return The decoded string.
     */
    override fun decodeString(encodedString: String): String {
        // Using Android's Uri class to decode the string
        return Uri.decode(encodedString)
    }
}