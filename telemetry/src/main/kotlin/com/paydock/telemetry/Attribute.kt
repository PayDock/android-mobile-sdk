package com.paydock.telemetry

/** One record attribute: an allowlisted key and its typed value. */
internal data class Attribute(val key: String, val value: AttributeValue)
