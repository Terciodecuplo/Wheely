package com.jmblfma.wheely.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int,
    val nickname:String = "Yabadabadú",
    val firstName:String = "Pedro",
    val lastName: String = "Picapiedra",
    val email:String = "flintstones@stoneage.bc",
    val dateOfBirth:String = "01/01/00"
)
