package com.jmblfma.wheely.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate


@Entity(
    tableName = "vehicles",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("userId"),
            childColumns = arrayOf("ownerId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val vehicleId: Int,
    @ColumnInfo(index = true) val ownerId: Int,
    val name: String,
    val brand: String,
    val model: String,
    val year: String,
    val horsepower: Int,
    val dateAdded: String = LocalDate.now().toString()
)
