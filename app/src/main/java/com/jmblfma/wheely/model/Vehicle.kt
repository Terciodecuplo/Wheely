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
    @PrimaryKey(autoGenerate = true) val vehicleId: Int = 0,
    @ColumnInfo(index = true) val ownerId: Int = 0,
    val name: String = "NAME_HERE",
    val brand: String = "BRAND_HERE",
    val model: String = "MODEL_HERE",
    val year: String = "YEAR_HERE",
    val horsepower: Int = 0,
    val dateAdded: String = LocalDate.now().toString(),
    val image: String = "IMG_URI"
) {
    override fun toString(): String {
        return name
    }
}
