package com.jmblfma.wheely.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int,
    val nickname: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val dateOfBirth: String,
    val profileImage: String?,
    val profileBanner: String?
)
