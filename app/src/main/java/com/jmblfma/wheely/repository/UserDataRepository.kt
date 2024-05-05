package com.jmblfma.wheely.repository

import android.util.Log
import com.jmblfma.wheely.data.RoomDatabaseBuilder
import com.jmblfma.wheely.data.UserDao
import com.jmblfma.wheely.model.User

class UserDataRepository {
    private val roomsDB = RoomDatabaseBuilder.sharedInstance
    private val userDao: UserDao = roomsDB.userDao()

    companion object {
        private val instance: UserDataRepository by lazy { UserDataRepository() }
        val sharedInstance: UserDataRepository = instance
    }

    suspend fun fetchUsers():List<User> = userDao.fetchUsers()

    suspend fun addUser(user: User, onResult: (Boolean) -> Unit) {
        if (userDao.checkEmailExists(user.email)) {
            onResult(false)
        } else {
            try {
                val id = userDao.postUser(user)
                if (id == -1L) { // The -1 is returned by Room if the user can't be inserted
                    throw Exception("Failed to add a user")
                }
            } catch (e: Exception) {
                throw Exception("Database error: ${e.message}")
            }
        }
    }

    suspend fun getUserByEmail(email: String) :User? = userDao.getUserByEmail(email)

    suspend fun deleteUser(email: String): Int? = userDao.deleteUser(email)

    suspend fun updateUserBanner(userId: Int, bannerPath: String?, onResult: (Int) -> Unit) {
        try {
            val result = userDao.updateUserBanner(userId, bannerPath)
            onResult(result) // The result will be the number of rows affected by the UPDATE
        } catch (e: Exception) {
            throw Exception("Database error: ${e.message}")
        }
    }

    suspend fun updateUserPersonalInfo(
        userId: Int,
        newNickname: String?,
        newFirstName: String?,
        newLastName: String?,
        newDateOfBirth: String?,
        newProfileImage: String?,
        onResult: (Int) -> Unit
    ) {
        try {
            val result = userDao.updateUserPersonalInfo(
                userId,
                newNickname,
                newFirstName,
                newLastName,
                newDateOfBirth,
                newProfileImage
            )
            onResult(result)
        } catch (e: Exception) {
            throw Exception("Database error, can't update user personal info: ${e.message}")
        }
    }

    suspend fun checkUserExists(email: String, onResult: (Boolean) -> Unit) {
        try {
            val user = userDao.checkEmailExists(email)
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
            throw Exception("Email exists error: ${e.message}")
        }
    }
}