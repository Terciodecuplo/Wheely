package com.jmblfma.wheely.model

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
}

