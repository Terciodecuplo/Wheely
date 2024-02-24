package com.jmblfma.wheely.model

data class PointData(
    val trackId: Int,
    val pointTimeLocation: Long,
    val velocityPoint: Double,
    val positionPoint: String,  //Need to confirm the type
    val altitudePoint: Double,
    val inclinationPoint: Double
) {}
