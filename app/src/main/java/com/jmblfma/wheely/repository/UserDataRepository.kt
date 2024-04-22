package com.jmblfma.wheely.repository

import android.database.sqlite.SQLiteConstraintException
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

    suspend fun addUser(user: User) : Boolean{
        return try {
            userDao.insertUser(user)
            true

        } catch (e: SQLiteConstraintException){
            false
            //throw Exception("Database error: ${e.message}")
        }
    }
}