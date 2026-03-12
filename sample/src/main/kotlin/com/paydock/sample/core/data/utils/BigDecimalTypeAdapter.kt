package com.paydock.sample.core.data.utils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.paydock.core.utils.toSafeAmount
import java.math.BigDecimal

/**
 * TypeAdapter for BigDecimal that serializes/deserializes as a plain string.
 * Normalizes to 2 decimal places so amounts created from Double or with floating-point
 * precision issues serialize/deserialize correctly (e.g. "89.99" not "89.98999999999...").
 */
class BigDecimalTypeAdapter : TypeAdapter<BigDecimal>() {
    override fun write(out: JsonWriter, value: BigDecimal?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toSafeAmount().toPlainString())
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
                if (stringValue.isEmpty()) null else stringValue.toBigDecimal().toSafeAmount()
            }

            JsonToken.NUMBER -> {
                val numberValue = `in`.nextString()
                numberValue.toBigDecimal().toSafeAmount()
            }

            else -> {
                `in`.skipValue()
                null
            }
        }
    }
}
