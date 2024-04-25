package com.jmblfma.wheely.repository

import com.jmblfma.wheely.data.RoomDatabaseBuilder
import com.jmblfma.wheely.data.UserDao
import com.jmblfma.wheely.model.User

class UserDataRepository() {
    private val roomsDB = RoomDatabaseBuilder.database
    private val userDao: UserDao = roomsDB.userDao()

    companion object {
        private val instance: UserDataRepository by lazy { UserDataRepository() }
        val sharedInstance: UserDataRepository
            get() = instance
    }

    suspend fun addUser(user: User, onResult: (Boolean) -> Unit) {
        if(userDao.checkEmailExists(user.email)){
            onResult(false)
        } else {
            try {
                val id = userDao.insertUser(user)
                if (id == -1L) { // The -1 is returned by Room if the user can't be inserted
                    throw Exception("Failed to add a user")
                }
            } catch (e: Exception) {
                throw Exception("Database error: ${e.message}")
            }
        }
    }

    suspend fun getUserByEmail(email: String, onResult: (User?) -> Unit) {
        try {
            val user = userDao.getUserByEmail(email)
            onResult(user)
        } catch (e: Exception) {
            onResult(null)
            throw Exception("Database error: ${e.message}")
        }
    }
}