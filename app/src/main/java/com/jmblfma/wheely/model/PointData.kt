package com.jmblfma.wheely.model

import java.time.ZonedDateTime

data class PointData(
    val timestamp: ZonedDateTime,
    val latitude: Double,
    val longitude: String,  //Need to confirm the type
    val altitudePoint: Double,
    val inclinationPoint: Double,
    val speed: Double
) {}
