package com.jmblfma.wheely.model

import java.time.ZonedDateTime

data class Track(
    val trackId: Int,
    val drivenBy: User,
    val vehicleUsed: Vehicle,
    val name: String,
    val generalLocation: String,
    val creationDate: ZonedDateTime,
    val trackData: ArrayList<TrackPoint>,
    val trackDifficulty: String,
    val trackSummary: DataSummary
) {}
