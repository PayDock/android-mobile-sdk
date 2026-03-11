package com.paydock.core.utils

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utilities for safe amount handling to avoid floating-point precision issues.
 *
 * When amounts come from [Double] (e.g. cart totals, prices) or imprecise [BigDecimal],
 * conversion can produce invalid strings (e.g. "89.98999999999999...") that APIs reject.
 * Use [Double.toSafeAmount] or [BigDecimal.toSafeAmount] to normalize to 2 decimal places.
 */

/**
 * Converts a [Double] to a [BigDecimal] with 2 decimal places.
 * Use this instead of [BigDecimal.valueOf] or [BigDecimal] constructor when the value
 * originates from floating-point math (e.g. cart totals, sumOf).
 */
fun Double.toSafeAmount(): BigDecimal =
    BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP)

/**
 * Normalizes a [BigDecimal] to 2 decimal places.
 * Use when the value may have been created from [Double] or have excess precision.
 */
fun BigDecimal.toSafeAmount(): BigDecimal =
    setScale(2, RoundingMode.HALF_UP)
