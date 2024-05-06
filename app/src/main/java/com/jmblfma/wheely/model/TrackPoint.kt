package com.jmblfma.wheely.model

import android.location.Location
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "trackpoints",
    foreignKeys = [
        ForeignKey(
            entity = Track::class,
            parentColumns = ["trackId"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TrackPoint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trackId: Int = 0,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val bearing: Float? = null // optional bearing data
) {
    fun hasBearing(): Boolean {
        return bearing != null
    }

    companion object {
        fun mapToTrackPoint(location: Location): TrackPoint {
            return TrackPoint(
                timestamp = location.time,
                latitude =  location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                speed = location.speed,
                bearing = location.bearing
            )
        }

        fun mapToLocation(trackPoint: TrackPoint): Location {
            val location = Location("trackPoint_to_location_mapper")
            location.time = trackPoint.timestamp
            location.latitude = trackPoint.latitude
            location.longitude = trackPoint.longitude
            location.altitude = trackPoint.altitude
            location.speed = trackPoint.speed
            if (trackPoint.bearing != null) {
                location.bearing = trackPoint.bearing
            }
            return location
        }
    }
}

