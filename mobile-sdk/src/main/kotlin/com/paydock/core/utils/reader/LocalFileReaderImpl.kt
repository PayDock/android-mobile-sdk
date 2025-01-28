package com.paydock.core.utils.reader

import android.content.Context
import com.paydock.core.presentation.extensions.readTextFromAsset
import com.paydock.core.presentation.extensions.readTextFromResources

/**
 * Implementation of the `LocalFileReader` interface that uses the Android context
 * to read files from assets and resources.
 *
 * @param context The Android context used to access assets and resources.
 */
class LocalFileReaderImpl(private val context: Context) : LocalFileReader {

    /**
     * Reads the text content from an asset file using the context's `readTextFromAsset()` function.
     *
     * @param fileName The name of the asset file.
     * @return The text content of the asset file.
     */
    override fun readFileFromAssets(fileName: String): String {
        return context.readTextFromAsset(fileName)
    }

    /**
     * Reads the text content from a resource file using the context's `readTextFromResources()` function.
     *
     * @param fileName The name of the resource file.
     * @return The text content of the resource file.
     */
    override fun readFileFromResources(fileName: String): String {
        return context.readTextFromResources(fileName)
    }
}