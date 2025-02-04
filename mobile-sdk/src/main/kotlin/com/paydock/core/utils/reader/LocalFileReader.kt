package com.paydock.core.utils.reader

/**
 * Interface for reading files from assets and resources.
 */
interface LocalFileReader {
    /**
     * Reads the text content from an asset file.
     *
     * @param fileName The name of the asset file.
     * @return The text content of the asset file.
     */
    fun readFileFromAssets(fileName: String): String

    /**
     * Reads the text content from a resource file.
     *
     * @param fileName The name of the resource file.
     * @return The text content of the resource file.
     */
    fun readFileFromResources(fileName: String): String
}