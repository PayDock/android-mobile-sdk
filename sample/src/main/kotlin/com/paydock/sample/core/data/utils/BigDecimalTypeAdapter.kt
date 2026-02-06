package com.paydock.sample.core.data.utils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * TypeAdapter for BigDecimal that serializes/deserializes as a plain string.
 * When writing, normalizes to at most 2 decimal places so amounts created from
 * Double (e.g. 89.99) serialize as "89.99" instead of "89.98999999999999...",
 * which APIs (e.g. order.amount) reject.
 */
class BigDecimalTypeAdapter : TypeAdapter<BigDecimal>() {
    override fun write(out: JsonWriter, value: BigDecimal?) {
        if (value == null) {
            out.nullValue()
        } else {
            val toWrite = if (value.scale() > 2) {
                value.setScale(2, RoundingMode.HALF_UP)
            } else {
                value
            }
            out.value(toWrite.toPlainString())
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
