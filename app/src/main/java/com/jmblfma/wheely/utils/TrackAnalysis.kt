package com.jmblfma.wheely.utils

import android.location.Location
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TrackAnalysis {
    const val FAILED_CALC_MSG = "NaN"
    // TODO velocidad media total, velocidad maxima, duración total, número de rutas
    fun formatSpeedInKmh(speedInMs: Double?): String {
        return speedInMs?.let { String.format("%.2f km/h", convertSpeedToKmh(speedInMs)) } ?: FAILED_CALC_MSG
    }
    fun formatDistanceInKm(distanceInMeters: Double?): String {
        return distanceInMeters?.let { String.format("%.2f km", convertDistanceToKm(distanceInMeters)) } ?: FAILED_CALC_MSG
    }
    fun formatAltitudeInMeters(altitude: Double?): String {
        return altitude?.let { String.format("%.0f m", altitude) }  ?: FAILED_CALC_MSG
    }

    // SINGLE TRACK ANALYSIS
    fun computeAverageSpeed(trackData: List<TrackPoint>): Double {
        return trackData.map { it.speed }.average()
    }
    private fun convertSpeedToKmh(speedInMs: Double): Double {
        // m / s * 3600s / 1000 m km / h
        return speedInMs * 3600f/1000
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
    fun getMaxSpeedInMs(trackData: List<TrackPoint>): Double? {
        return trackData.maxByOrNull { it.speed }?.speed?.toDouble()
    }

    // TRACK BATCH ANALYSIS
    fun getTracksAverageSpeedInKmh(trackBatch: List<Track>): String {
        // if a particular track's ave. speed is NULL; skips this value to compute the average (ie mapNotNull)
        val averageSpeed = trackBatch.mapNotNull { it.averageSpeed }.average()
        return formatSpeedInKmh(averageSpeed)
    }
    fun getTracksTotalDistanceInKm(trackBatch: List<Track>): String {
        // same logic as for average speed
        val totalDistance = trackBatch.mapNotNull { it.totalDistance }.sum()
        return formatDistanceInKm(totalDistance)
    }
    fun getTracksTotalDuration(trackBatch: List<Track>): String {
        var totalDuration = Duration.ZERO
        trackBatch.forEach {
            if (it.startTime != null && it.endTime != null) {
                totalDuration += computeDurationBetweenTimestamps(it.startTime!!, it.endTime!!)
            }
        }
        return formatDuration(totalDuration, 0)
    }
    fun getTracksMaxSpeed(trackBatch: List<Track>): String {
        val maxSpeed = trackBatch.mapNotNull { it.maxSpeed }.max()
        return formatSpeedInKmh(maxSpeed)
    }

    // DATE TIME UTILS
    private const val DATE_FORMAT_PATTERN_SYS = "yyyy-MM-dd" // e.g. 2024-05-11"
    private const val TIME_FORMAT_PATTERN = "HH:mm" // e.g. 21:49
    private fun convertTimestampToFormattedDateTime(timestamp: Long?, format: String): String {
        return timestamp?.let {
            val dateTime =  LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            dateTime.format(DateTimeFormatter.ofPattern(format))
        } ?: FAILED_CALC_MSG
    }
    fun convertTimestampToTime(timestamp: Long?): String {
        return convertTimestampToFormattedDateTime(timestamp, TIME_FORMAT_PATTERN)
    }
    fun convertTimestampToDate(timestamp: Long?): String {
        return convertTimestampToFormattedDateTime(timestamp, DATE_FORMAT_PATTERN_SYS)
    }
    private fun computeDurationFromMillis(timeMillis: Long): Duration {
        return Duration.ofMillis(timeMillis)
    }
    private fun computeDurationBetweenTimestamps(startTimestamp: Long, endTimestamp: Long): Duration {
        val startInstant = Instant.ofEpochMilli(startTimestamp)
        val endInstant = Instant.ofEpochMilli(endTimestamp)
        return Duration.between(startInstant, endInstant)
    }
    fun formatDurationFromMillis(timeMillis: Long): String {
        return formatDuration(computeDurationFromMillis(timeMillis))
    }
    fun formatDurationBetweenTimestamps(startTimestamp: Long?, endTimestamp: Long?): String {
        return if (startTimestamp != null && endTimestamp != null) {
            formatDuration(computeDurationBetweenTimestamps(startTimestamp, endTimestamp), 1)
        } else {
            FAILED_CALC_MSG
        }
    }
    private fun formatDuration(duration: Duration, mode: Int = 2): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60 // % 60 solves: 210/60 = 3(%=.5)  .5*60 = 30 min
        val seconds = duration.seconds % 60
        return when(mode) {
            0 -> {
                "${hours}h"
            }
            1 -> {
                "${hours}h ${minutes}min"
            }
            else -> {
                "${hours}h ${minutes}min ${seconds}s"
            }
        }
    }
}