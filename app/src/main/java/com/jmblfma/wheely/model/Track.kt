package com.jmblfma.wheely.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.jmblfma.wheely.data.Difficulty
import com.jmblfma.wheely.utils.TrackAnalysis

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
    var trackData: List<TrackPoint>? = null
    var startTime: Long? = null
    var endTime: Long? = null
    var averageSpeed: Double? = null
    var totalDistance: Double? = null
    var maxAltitude: Double? = null
    var maxSpeed: Double? = null
    var generalLocation: String? = null // TODO look at Geocoder implementation
    var routePreviewImageURI: String? = null

    fun computeTrackData(trackData: List<TrackPoint>) {
        this.trackData = trackData
        this.startTime = trackData.first().timestamp
        this.endTime = trackData.last().timestamp
        this.averageSpeed = TrackAnalysis.computeAverageSpeed(trackData)
        this.totalDistance = TrackAnalysis.calculateTotalDistanceInMeters(trackData)
        this.maxAltitude = TrackAnalysis.getMaxAltitudeInMeters(trackData)
        this.maxSpeed = TrackAnalysis.getMaxSpeedInMs(trackData)
    }

    // FORMATTED STATS METHODS
    fun getFormattedDate(): String {
        return TrackAnalysis.convertTimestampToDate(this.startTime)
    }
    fun getFormattedDuration(): String {
        return TrackAnalysis.formatDurationBetweenTimestamps(this.startTime, this.endTime)
    }
    fun getFormattedTime(isStart: Boolean): String {
        return if (isStart) {
            TrackAnalysis.convertTimestampToTime(this.startTime)
        } else {
            TrackAnalysis.convertTimestampToTime(this.endTime)
        }
    }
    fun getFormattedDateTime(): String {
        return TrackAnalysis.convertTimestampToDateTime(this.startTime)
    }
    fun getFormattedAverageSpeedInKmh(): String {
        return TrackAnalysis.formatSpeedInKmh(this.averageSpeed)
    }
    fun getFormattedDistanceInKm(): String {
        return TrackAnalysis.formatDistanceInKm(this.totalDistance)
    }
    fun getFormattedMaxAltitudInMeters(): String {
        return TrackAnalysis.formatAltitudeInMeters(this.maxAltitude)
    }
    fun getFormattedMaxSpeedInKmh(): String {
        return TrackAnalysis.formatSpeedInKmh(this.maxSpeed)
    }
}

