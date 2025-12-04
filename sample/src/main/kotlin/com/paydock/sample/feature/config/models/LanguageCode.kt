package com.paydock.sample.feature.config.models

/**
 * Supported language codes for widget configurations.
 * Uses ISO 639-1 language codes.
 */
enum class LanguageCode(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Spanish"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    ITALIAN("it", "Italian"),
    PORTUGUESE("pt", "Portuguese"),
    JAPANESE("ja", "Japanese"),
    CHINESE("zh", "Chinese"),
    KOREAN("ko", "Korean"),
    DUTCH("nl", "Dutch"),
    RUSSIAN("ru", "Russian"),
    ARABIC("ar", "Arabic");

    companion object {
        fun fromCode(code: String): LanguageCode? {
            return entries.find { it.code == code }
        }
    }
}

