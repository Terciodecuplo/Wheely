package com.jmblfma.wheely.repository

import androidx.room.Dao
import androidx.room.Insert
import com.jmblfma.wheely.model.User

@Dao
interface UserDao {
    @Insert
    fun insertUser(user: User): Long
}