package io.github.mklkj.kommunicator.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

fun LocalDate.Companion.now(): LocalDate {
    return Clock.System.now().toLocalDateTime(TimeZone.UTC).date
}

fun Instant.toLocalDate(tz: TimeZone = TimeZone.UTC): LocalDate {
    return toLocalDateTime(tz).date
}

fun Long.toInstant(): Instant {
    return Instant.fromEpochMilliseconds(this)
}

fun Long.toLocalDate(): LocalDate {
    return toInstant().toLocalDate()
}

fun LocalDate.getMillis(tz: TimeZone = TimeZone.UTC): Long {
    return atStartOfDayIn(tz).toEpochMilliseconds()
}
