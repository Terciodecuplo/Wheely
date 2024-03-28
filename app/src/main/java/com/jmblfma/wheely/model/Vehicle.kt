package com.jmblfma.wheely.model

import java.time.ZonedDateTime

data class Vehicle(
    val vehicleId: Int,
    val owner: User,
    val name: String,
    val brand: String,
    val model: String,
    val year: String,
    val horsepower: Int,
    val dateAdded: ZonedDateTime
) {}
