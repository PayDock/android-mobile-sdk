package com.paydock.telemetry

/**
 * `yyyy-MM-ddTHH:mm:ss.SSSZ` for a UTC epoch-ms value, by integer arithmetic only (days to civil date), with no time
 * zone, calendar or formatter class (UtcOnlyTest) and no java.time (minSdk 24, no desugaring in :telemetry).
 */
internal object UtcTimestamp {
    private const val MILLIS_PER_SECOND = 1_000L
    private const val SECONDS_PER_MINUTE = 60L
    private const val MINUTES_PER_HOUR = 60L
    private const val HOURS_PER_DAY = 24L
    private const val MILLIS_PER_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE
    private const val MILLIS_PER_HOUR = MILLIS_PER_MINUTE * MINUTES_PER_HOUR
    private const val MILLIS_PER_DAY = MILLIS_PER_HOUR * HOURS_PER_DAY

    // Days to civil date (proleptic Gregorian), counting years from March so the leap day ends a year.
    private const val DAYS_FROM_0000_03_01_TO_EPOCH = 719_468L
    private const val DAYS_PER_ERA = 146_097L // 400 years
    private const val YEARS_PER_ERA = 400L
    private const val DAYS_PER_4_YEARS = 1_460L
    private const val DAYS_PER_100_YEARS = 36_524L
    private const val LAST_DAY_OF_ERA = 146_096L
    private const val DAYS_PER_YEAR = 365L
    private const val LEAP_EVERY = 4L
    private const val CENTURY = 100L
    private const val MONTH_SPAN_DAYS = 153L // five months from March: 31 + 30 + 31 + 30 + 31
    private const val MONTH_SPAN_MONTHS = 5L
    private const val MONTH_ROUNDING = 2L
    private const val MARCH = 3L
    private const val JANUARY_OFFSET = 9L // months from March to January of the next year
    private const val FEBRUARY = 2L
    private const val MARCH_BASED_JANUARY = 10L

    private const val YEAR_DIGITS = 4
    private const val FIELD_DIGITS = 2
    private const val MILLIS_DIGITS = 3

    fun iso8601(epochMillis: Long): String {
        val days = epochMillis.floorDiv(MILLIS_PER_DAY)
        val millisOfDay = epochMillis.mod(MILLIS_PER_DAY)

        val shifted = days + DAYS_FROM_0000_03_01_TO_EPOCH
        val era = shifted.floorDiv(DAYS_PER_ERA)
        val dayOfEra = shifted - era * DAYS_PER_ERA
        val yearOfEra = (
            dayOfEra - dayOfEra / DAYS_PER_4_YEARS + dayOfEra / DAYS_PER_100_YEARS - dayOfEra / LAST_DAY_OF_ERA
            ) / DAYS_PER_YEAR
        val dayOfYear = dayOfEra - (DAYS_PER_YEAR * yearOfEra + yearOfEra / LEAP_EVERY - yearOfEra / CENTURY)
        val marchMonth = (MONTH_SPAN_MONTHS * dayOfYear + MONTH_ROUNDING) / MONTH_SPAN_DAYS
        val day = dayOfYear - (MONTH_SPAN_DAYS * marchMonth + MONTH_ROUNDING) / MONTH_SPAN_MONTHS + 1
        val month = if (marchMonth < MARCH_BASED_JANUARY) marchMonth + MARCH else marchMonth - JANUARY_OFFSET
        val year = yearOfEra + era * YEARS_PER_ERA + if (month <= FEBRUARY) 1 else 0

        val hour = millisOfDay / MILLIS_PER_HOUR
        val minute = millisOfDay % MILLIS_PER_HOUR / MILLIS_PER_MINUTE
        val second = millisOfDay % MILLIS_PER_MINUTE / MILLIS_PER_SECOND
        val millis = millisOfDay % MILLIS_PER_SECOND

        return buildString {
            append(year.padded(YEAR_DIGITS)).append('-')
            append(month.padded(FIELD_DIGITS)).append('-')
            append(day.padded(FIELD_DIGITS)).append('T')
            append(hour.padded(FIELD_DIGITS)).append(':')
            append(minute.padded(FIELD_DIGITS)).append(':')
            append(second.padded(FIELD_DIGITS)).append('.')
            append(millis.padded(MILLIS_DIGITS)).append('Z')
        }
    }

    private fun Long.padded(digits: Int): String = toString().padStart(digits, '0')
}
