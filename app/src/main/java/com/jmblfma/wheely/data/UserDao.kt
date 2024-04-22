package com.jmblfma.wheely.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.jmblfma.wheely.model.User

@Dao
interface UserDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
}