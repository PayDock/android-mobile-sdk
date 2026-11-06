package com.paydock.telemetry

/** A record attribute's typed value; each kind encodes as its OTLP AnyValue form. */
internal sealed interface AttributeValue {
    data class Str(val value: String) : AttributeValue // OTLP stringValue

    data class Num(val value: Long) : AttributeValue // OTLP intValue, written as a decimal string

    data class Bool(val value: Boolean) : AttributeValue // OTLP boolValue
}
