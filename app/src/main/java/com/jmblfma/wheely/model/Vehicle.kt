package com.jmblfma.wheely.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.time.ZonedDateTime
@Parcelize
data class Vehicle(
    val vehicleId: Int,
    val owner: @RawValue User,
    val name: String,
    val brand: String,
    val model: String,
    val year: String,
    val horsepower: Int,
    val dateAdded: ZonedDateTime
) : Parcelable {}
