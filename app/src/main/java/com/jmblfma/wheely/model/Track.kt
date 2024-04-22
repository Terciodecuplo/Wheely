package com.jmblfma.wheely.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity(tableName = "tracks")
data class Track(
    val trackData: List<TrackPoint>,
    @PrimaryKey(autoGenerate = true) val trackId: Int = 0,
    val userId: Int = 1,
    val vehicleUsed: Vehicle? = null,
    val name: String = "DefaultTrack",
    val generalLocation: String = "-",
    val creationTimestamp: ZonedDateTime = ZonedDateTime.now(),
    val difficulty: String = "-"
) {
    val averageSpeed = computeAverageSpeed(trackData)
    val startTime = trackData.first().timestamp
    val endTime = trackData.last().timestamp

    companion object {
        fun computeAverageSpeed(trackData: List<TrackPoint>): Double {
            return trackData.map { it.speed }.average()
        }
    }
}

