package com.jmblfma.wheely.utils

import android.location.Location
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TrackAnalysis {
    const val FAILED_CALC_MSG = "0"
    const val FAILED_DATE_TIME_MSG = "DATE_PARSER_FAILED"

    fun formatSpeedInKmh(speedInMs: Double?, decimalPlaces: Int = 2): String {
        return speedInMs?.let {
            String.format(
                "%.${decimalPlaces}f km/h",
                convertSpeedToKmh(speedInMs)
            )
        } ?: "$FAILED_CALC_MSG km/h"
    }

    fun formatDistanceInKm(distanceInMeters: Double?, decimalPlaces: Int = 2): String {
        return distanceInMeters?.let {
            String.format(
                "%.${decimalPlaces}f km",
                convertDistanceToKm(distanceInMeters)
            )
        } ?: "$FAILED_CALC_MSG km"
    }

    fun formatAltitudeInMeters(altitude: Double?, decimalPlaces: Int = 0): String {
        return altitude?.let { String.format("%.${decimalPlaces}f m", altitude) } ?: FAILED_CALC_MSG
    }


    // SINGLE TRACK ANALYSIS
    fun computeAverageSpeed(trackData: List<TrackPoint>): Double {
        return trackData.map { it.speed }.average()
    }

    private fun convertSpeedToKmh(speedInMs: Double): Double {
        // m / s * 3600s / 1000 m km / h
        return speedInMs * 3600f / 1000
    }

    fun calculateTotalDistanceInMeters(trackData: List<TrackPoint>): Double {
        var totalDistance = 0f
        for (i in 1 until trackData.size) {
            totalDistance += calculateDistanceBetweenPoints(trackData[i - 1], trackData[i])
        }
        return totalDistance.toDouble()
    }

    private fun calculateDistanceBetweenPoints(origin: TrackPoint, destination: TrackPoint): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            origin.latitude, origin.longitude,
            destination.latitude, destination.longitude,
            results
        )
        return results[0]  // Distance in meters
    }

    private fun convertDistanceToKm(distance: Double): Double {
        return distance / 1000
    }

    fun getMaxAltitudeInMeters(trackData: List<TrackPoint>): Double? {
        return trackData.maxByOrNull { it.altitude }?.altitude
    }

    fun calculateElevationInMeters(trackData: List<TrackPoint>): Double? {
        if (trackData.isEmpty()) return null

        val maxAltitude = trackData.maxByOrNull { it.altitude }?.altitude
        val minAltitude = trackData.minByOrNull { it.altitude }?.altitude

        return if (maxAltitude != null && minAltitude != null) {
            maxAltitude - minAltitude
        } else {
            null
        }
    }

    fun getMaxSpeedInMs(trackData: List<TrackPoint>): Double? {
        return trackData.maxByOrNull { it.speed }?.speed?.toDouble()
    }

    // TRACK BATCH ANALYSIS
    fun getTracksAverageSpeedInKmh(trackBatch: List<Track>): String {
        // if a particular track's ave. speed is NULL; skips this value to compute the average (ie mapNotNull)
        val speeds = trackBatch.mapNotNull { it.averageSpeed }
        val averageSpeed = if (speeds.isNotEmpty()) speeds.average() else null
        return formatSpeedInKmh(averageSpeed, 0)
    }

    // TODO review potential crashes when using applying mapNotNull/maxOrnull
    // controlled in UI as well so safe for now
    fun getTracksTotalDistanceInKm(trackBatch: List<Track>): String {
        // same logic as for average speed
        val totalDistance = trackBatch.mapNotNull { it.totalDistance }.sum() // handles emptyList fine
        return formatDistanceInKm(totalDistance, 0)
    }

    fun getLongestTrackInKm(trackBatch: List<Track>): String {
        val longestTrack = trackBatch.mapNotNull { it.totalDistance }.maxOrNull()
        return formatDistanceInKm(longestTrack, 0)
    }

    fun getTracksTotalDuration(trackBatch: List<Track>, showMinutes: Boolean = false): String {
        var totalDuration = Duration.ZERO
        trackBatch.forEach {
            if (it.startTime != null && it.endTime != null) {
                totalDuration += computeDurationBetweenTimestamps(it.startTime!!, it.endTime!!)
            }
        }
        val mode = if (showMinutes) 1 else 0
        return formatDuration(totalDuration, mode)
    }

    fun getTracksMaxSpeed(trackBatch: List<Track>): String {
        val maxSpeed = trackBatch.mapNotNull { it.maxSpeed }.maxOrNull()
        return formatSpeedInKmh(maxSpeed, 0)
    }

    fun getTracksMaxAltitude(trackBatch: List<Track>): String {
        val maxAltitude = trackBatch.mapNotNull { it.maxAltitude }.maxOrNull()
        return formatAltitudeInMeters(maxAltitude, 0)
    }

    fun getTracksMaxDuration(trackBatch: List<Track>): String {
        val maxDuration = trackBatch.mapNotNull {
            if (it.startTime != null && it.endTime != null) {
                computeDurationBetweenTimestamps(it.startTime!!, it.endTime!!)
            } else {
                null
            }
        }.maxOrNull()
        return formatDuration(maxDuration, 1)
    }

    // DATE TIME UTILS
    private const val DATE_FORMAT_PATTERN_SYS = "yyyy-MM-dd" // e.g. 2024-05-11
    private const val TIME_FORMAT_PATTERN = "HH:mm" // e.g. 21:49


    private fun convertTimestampToFormattedDateTime(timestamp: Long?, format: String): String {
        return timestamp?.let {
            val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            val formatter = DateTimeFormatter.ofPattern(format, Locale.getDefault())
            dateTime.format(formatter)
        } ?: FAILED_DATE_TIME_MSG
    }

    fun convertTimestampToTime(timestamp: Long?): String {
        return convertTimestampToFormattedDateTime(timestamp, TIME_FORMAT_PATTERN)
    }

    fun convertTimestampToDate(timestamp: Long?): String {
        return convertTimestampToFormattedDateTime(timestamp, DATE_FORMAT_PATTERN_SYS)
    }

    fun convertTimestampToDateTime(timestamp: Long?): String {
        val locale = Locale.getDefault()
        val localizedDateTimePattern = getLocalizedDateTimePattern(locale)
        return convertTimestampToFormattedDateTime(timestamp, localizedDateTimePattern)
    }

    // TODO add patterns for different languages as needed
    // for anything other than ES it defaults to english
    private fun getLocalizedDateTimePattern(locale: Locale): String {
        return when (locale.language) {
            "es" -> "d 'de' MMMM 'de' yyyy 'a las' HH:mm"
            else -> "MMMM d, yyyy 'at' hh:mm a"
        }
    }

    private fun computeDurationFromMillis(timeMillis: Long): Duration {
        return Duration.ofMillis(timeMillis)
    }

    private fun computeDurationBetweenTimestamps(
        startTimestamp: Long,
        endTimestamp: Long
    ): Duration {
        val startInstant = Instant.ofEpochMilli(startTimestamp)
        val endInstant = Instant.ofEpochMilli(endTimestamp)
        return Duration.between(startInstant, endInstant)
    }

    fun formatDurationFromMillis(timeMillis: Long?): String {
        return if (timeMillis != null) {
            formatDuration(computeDurationFromMillis(timeMillis))
        } else {
            FAILED_DATE_TIME_MSG
        }
    }

    fun formatDurationBetweenTimestamps(startTimestamp: Long?, endTimestamp: Long?, withSeconds: Boolean = false): String {
        return if (startTimestamp != null && endTimestamp != null) {
            val mode = if (withSeconds) 2 else 1
            formatDuration(computeDurationBetweenTimestamps(startTimestamp, endTimestamp), mode)
        } else {
            FAILED_DATE_TIME_MSG
        }
    }

    private fun formatDuration(duration: Duration?, mode: Int = 2): String {
        val currentDuration = duration ?: Duration.ZERO
        val hours = currentDuration.toHours()
        val minutes = currentDuration.toMinutes() % 60 // % 60 solves: 210/60 = 3(%=.5)  .5*60 = 30 min
        val seconds = currentDuration.seconds % 60
        return when (mode) {
            0 -> {
                "${hours} h"
            }

            1 -> {
                "${hours} h ${minutes} min"
            }

            else -> {
                "${hours} h ${minutes} min ${seconds} s"
            }
        }
    }
}