package io.github.mklkj.kommunicator.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

actual fun Instant.format(format: String): String = DateTimeFormatter
    .ofPattern(format)
    .format(toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
