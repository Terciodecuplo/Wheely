package com.jmblfma.wheely.utils

import java.time.Duration
import java.time.Instant

fun formatTime(timeMillis: Long): String {
    val duration = Duration.ofMillis(timeMillis)
    val hours = duration.toHours()
    val minutes = duration.toMinutes()
    val seconds = duration.seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun computeDuration(startTime: Long, endTime: Long): String {
    val duration = Duration.between(Instant.ofEpochMilli(startTime), Instant.ofEpochMilli(endTime))
    val hours = duration.toHours()
    val minutes = duration.toMinutes()
    val seconds = duration.seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}