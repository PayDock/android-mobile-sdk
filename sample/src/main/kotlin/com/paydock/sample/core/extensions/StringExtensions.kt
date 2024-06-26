package com.paydock.sample.core.extensions

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}

fun String.removeSuffixIgnoreCase(suffix: String): String {
    if (endsWith(suffix, ignoreCase = true)) {
        val prefixLength = length - suffix.length
        return substring(0, prefixLength)
    }
    return this
}

fun String.isValidHexCode(): Boolean {
    val hexCodeRegex = Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    return this.matches(hexCodeRegex)
}
