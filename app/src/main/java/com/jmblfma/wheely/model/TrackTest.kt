package com.jmblfma.wheely.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.time.ZonedDateTime
@Parcelize
data class TrackTest(
    val trackId: Int,
    val drivenBy: @RawValue User,
    val vehicleUsed: @RawValue Vehicle,
    val name: String,
    val generalLocation: String,
    val creationDate: ZonedDateTime,
    val trackData: @RawValue ArrayList<TrackPoint>,
    val trackDifficulty: String,
    val trackSummary: @RawValue DataSummary
) : Parcelable {}