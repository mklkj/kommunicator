package io.github.mklkj.kommunicator.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toInstant

fun timeAgoSince(date: LocalDateTime): String {
    val now = Clock.System.now()
    val duration = date.toInstant(TimeZone.UTC).periodUntil(now, TimeZone.UTC)
    val weeks = duration.days / 7

    return when {
        duration.years >= 2 -> "${duration.years} years ago"
        duration.years >= 1 -> "Last year"
        duration.months >= 2 -> "${duration.months} months ago"
        duration.months >= 1 -> "Last month"
        weeks >= 2 -> "$weeks weeks ago"
        weeks >= 1 -> "Last week"
        duration.days >= 2 -> "${duration.days} days ago"
        duration.days >= 1 -> "Yesterday"
        duration.hours >= 2 -> "${duration.hours} hours ago"
        duration.hours >= 1 -> "An hour ago"
        duration.minutes >= 2 -> "${duration.minutes} minutes ago"
        duration.minutes >= 1 -> "A minute ago"
        duration.seconds >= 3 -> "${duration.seconds} seconds ago"
        else -> "Just now"
    }
}
