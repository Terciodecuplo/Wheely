package com.jmblfma.wheely.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jmblfma.wheely.model.User

@Dao
interface UserDao {
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email LIMIT 1)")
    suspend fun checkEmailExists(email: String): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun postUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("UPDATE users SET profileBanner = COALESCE(:profileBanner, profileBanner) WHERE userId = :userId")
    suspend fun updateUserBanner(userId: Int, profileBanner: String) : Int

}