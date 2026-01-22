package com.paydock.sample.core.data.utils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.math.BigDecimal

/**
 * TypeAdapter for BigDecimal that serializes/deserializes as a plain string
 * to avoid floating-point precision issues.
 */
class BigDecimalTypeAdapter : TypeAdapter<BigDecimal>() {
    override fun write(out: JsonWriter, value: BigDecimal?) {
        if (value == null) {
            out.nullValue()
        } else {
            // Use toPlainString() to avoid scientific notation and preserve exact decimal representation
            out.value(value.toPlainString())
        }
    }

    override fun read(`in`: JsonReader): BigDecimal? {
        return when (`in`.peek()) {
            JsonToken.NULL -> {
                `in`.nextNull()
                null
            }
            JsonToken.STRING -> {
                val stringValue = `in`.nextString()
                if (stringValue.isEmpty()) null else BigDecimal(stringValue)
            }
            JsonToken.NUMBER -> {
                // Handle both integer and decimal numbers
                val numberValue = `in`.nextString()
                BigDecimal(numberValue)
            }
            else -> {
                `in`.skipValue()
                null
            }
        }
    }
}
