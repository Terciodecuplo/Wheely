package com.jmblfma.wheely.model

import android.location.Location
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.jmblfma.wheely.utils.computeDuration
import java.time.ZonedDateTime

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey(autoGenerate = true) val trackId: Int = 0,
    val name: String = "DefaultTrack"
) {
    @Ignore
    lateinit var trackData: List<TrackPoint>
    @Ignore
    val userId: Int = 1
    @Ignore
    val vehicleId: Int = 1
    @Ignore
    val generalLocation: String = "-"
    @Ignore
    val difficulty: String = "-"
    @Ignore
    val creationTimestamp: ZonedDateTime = ZonedDateTime.now()

    var averageSpeed: Double = 0.0
    var startTime: Long = 0
    var endTime: Long = 0

    companion object {
        fun build(
            trackId: Int = 0,
            name: String = "DefaultTrack",
            trackData: List<TrackPoint>
        ): Track {
            val track = Track(trackId, name)
            track.trackData = trackData
            track.averageSpeed = computeAverageSpeed(trackData)
            track.startTime = trackData.first().timestamp
            track.endTime = trackData.last().timestamp
            return track
        }
        fun computeAverageSpeed(trackData: List<TrackPoint>): Double {
            return trackData.map { it.speed }.average()
        }

        fun convertSpeedToKmh(speedInMs: Double): Double {
            // m / s * 3600s / 1000 m km / h
            return speedInMs * 3600f/1000
        }

        fun calculateTotalDistanceInMeters(trackData: List<TrackPoint>): Float {
            var totalDistance = 0f
            for (i in 1 until trackData.size) {
                totalDistance += calculateDistanceBetweenPoints(trackData[i - 1], trackData[i])
            }
            return totalDistance
        }
        fun calculateDistanceBetweenPoints(origin: TrackPoint, destination: TrackPoint): Float {
            val results = FloatArray(1)
            Location.distanceBetween(
                origin.latitude, origin.longitude,
                destination.latitude, destination.longitude,
                results
            )
            return results[0]  // Distance in meters
        }

        fun convertDistanceToKm(distance: Float): Float {
            return distance / 1000
        }

        fun formatDistanceInKm(distanceInMeters: Float): String {
            val distanceInKm = convertDistanceToKm(distanceInMeters)
            return String.format("%.2f km", distanceInKm)
        }

        fun formatSpeedInKmh(speedInMs: Double): String {
            val speedInKmh = convertSpeedToKmh(speedInMs)
            return String.format("%.2f km/h", speedInKmh)
        }
    }

    fun getFormattedDuration(): String {
        return computeDuration(this.startTime, this.endTime)
    }

    fun getFormattedAverageSpeedInKmh(): String {
        return formatSpeedInKmh(computeAverageSpeed(this.trackData))
    }

    fun getFormattedDistanceInKm(): String {
        return formatDistanceInKm(calculateTotalDistanceInMeters(this.trackData))
    }
}

