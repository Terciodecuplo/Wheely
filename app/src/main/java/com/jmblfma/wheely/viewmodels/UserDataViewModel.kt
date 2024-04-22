package com.jmblfma.wheely.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmblfma.wheely.data.RoomDatabaseBuilder
import com.jmblfma.wheely.data.UserDao
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.repository.UserDataRepository
import kotlinx.coroutines.launch

class UserDataViewModel : ViewModel(){
    private val userDao: UserDao = RoomDatabaseBuilder.database.userDao()
    private val repository = UserDataRepository.sharedInstance

    private val _userAdditionStatus = MutableLiveData<String?>()
    val userAdditionStatus : LiveData<String?>
        get() = _userAdditionStatus

    fun addUser(user: User){
        viewModelScope.launch{
            try {
                val success = repository.addUser(user)
                    if (success) {
                        _userAdditionStatus.postValue("User added successfully")
                    } else {
                        _userAdditionStatus.postValue("User with this email already exists")
                    }
            } catch (e: Exception) {
                    _userAdditionStatus.postValue("Error adding user: ${e.message}")
            }
        }

    }
}