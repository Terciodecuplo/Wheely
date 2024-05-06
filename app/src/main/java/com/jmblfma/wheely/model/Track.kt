package com.jmblfma.wheely.model

import android.location.Location
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.jmblfma.wheely.data.Difficulty
import com.jmblfma.wheely.utils.computeDuration
import java.time.ZonedDateTime

@Entity(
    tableName = "tracks",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["drivenByUserId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["vehicleId"],
            childColumns = ["vehicleUsedId"],
            onDelete = ForeignKey.SET_NULL // prevents track deletion when vehicle is deleted
        )
    ]
)
data class Track(
    @PrimaryKey(autoGenerate = true) val trackId: Int = 0,
    val name: String,
    val drivenByUserId: Int,
    val vehicleUsedId: Int?,
    val description: String?,
    val difficultyValue: Difficulty = Difficulty.UNKNOWN
) {
    @Ignore
    lateinit var trackData: List<TrackPoint>
    @Ignore // TODO look at Geocoder implementation
    val generalLocation: String = "-"
    @Ignore
    // TODO delete (likely redundant as this can be provided with a getter and startTime)
    val creationTimestamp: ZonedDateTime = ZonedDateTime.now()

    var averageSpeed: Double = 0.0
    var startTime: Long = 0
    var endTime: Long = 0

    fun computeTrackData(trackData: List<TrackPoint> = this.trackData) {
        this.trackData = trackData
        this.averageSpeed = computeAverageSpeed(this.trackData)
        this.startTime = trackData.first().timestamp
        this.endTime = trackData.last().timestamp
    }

    companion object {
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

    override fun toString(): String {
        return "Track(trackId=$trackId, name=$name, drivenByUserId=$drivenByUserId, vehicleUsedId=$vehicleUsedId, " +
                "description=$description, difficultyValue=$difficultyValue, averageSpeed=${getFormattedAverageSpeedInKmh()}, " +
                "duration=${getFormattedDuration()}, distance=${getFormattedDistanceInKm()}, generalLocation=$generalLocation)"
    }

    // TODO velocidad media total, velocidad maxima, duración total, número de rutas

}

