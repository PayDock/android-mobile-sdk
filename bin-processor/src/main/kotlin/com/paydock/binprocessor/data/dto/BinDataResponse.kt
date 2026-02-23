package com.paydock.binprocessor.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing the BIN data response structure.
 *
 * @property prefix2 Map of 2-digit prefixes to scheme codes (e.g., "4" -> "v", "41" -> "v")
 * @property prefix4 Map of 4-digit prefixes to scheme codes
 * @property prefix6 Map of 6-digit BIN prefixes to scheme codes
 * @property prefix8 Map of 8-digit BIN prefixes to scheme codes
 * @property ranges Range arrays for each prefix length
 * @property schemes Mapping of scheme codes to scheme names (e.g., "m" -> "mastercard", "v" -> "visa")
 * @property version Version number of the BIN data
 */
@Serializable
data class BinDataResponse(
    @SerialName("2") val prefix2: Map<String, String> = emptyMap(),
    @SerialName("4") val prefix4: Map<String, String> = emptyMap(),
    @SerialName("6") val prefix6: Map<String, String> = emptyMap(),
    @SerialName("8") val prefix8: Map<String, String> = emptyMap(),
    @SerialName("r") val ranges: BinRanges = BinRanges(),
    @SerialName("s") val schemes: Map<String, String> = emptyMap(),
    @SerialName("v") val version: Int = 1
)

/**
 * Data class representing BIN ranges for different prefix lengths.
 *
 * @property range2 List of 2-digit ranges [[start, end, schemeCode], ...]
 * @property range4 List of 4-digit ranges [[start, end, schemeCode], ...]
 * @property range6 List of 6-digit ranges [[start, end, schemeCode], ...]
 * @property range8 List of 8-digit ranges [[start, end, schemeCode], ...]
 */
@Serializable
data class BinRanges(
    @SerialName("2") val range2: List<List<String>> = emptyList(),
    @SerialName("4") val range4: List<List<String>> = emptyList(),
    @SerialName("6") val range6: List<List<String>> = emptyList(),
    @SerialName("8") val range8: List<List<String>> = emptyList()
)
