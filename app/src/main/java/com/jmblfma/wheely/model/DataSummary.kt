package com.jmblfma.wheely.model

data class DataSummary(
    val summaryId:Int,
    val trackId :Int,
    val elapsedTime: Long,
    val maxVelocity: Double,
    val averageVelocity: Double,
    val distanceRode: Double,
    val maxInclination: Double,
    val averageInclination: Double,
    val maxAltitude: Double
){}
