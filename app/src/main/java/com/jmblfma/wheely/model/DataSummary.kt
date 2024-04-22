package com.jmblfma.wheely.model


data class DataSummary(
    val summaryId:Int,
    val elapsedTime: Double,
    val maxSpeed: Double,
    val averageSpeed: Double,
    val distanceTraveled: Double,
    val maxInclination: Double,
    val averageInclination: Double,
    val maxAltitude: Double,
    val deltaAltitude: Double
)
