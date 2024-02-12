package io.github.mklkj.kommunicator.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDateFormatter

actual fun Instant.format(format: String): String {
    val dateFormatter = NSDateFormatter().apply {
        dateFormat = format
    }
    return dateFormatter.stringFromDate(toNSDate())
}
