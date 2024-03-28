package com.jmblfma.wheely.model

import org.osmdroid.util.GeoPoint

data class TrackPoint(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float
) {}

