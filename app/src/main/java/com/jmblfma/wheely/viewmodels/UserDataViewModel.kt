package com.jmblfma.wheely.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmblfma.wheely.data.RoomDatabaseBuilder
import com.jmblfma.wheely.data.UserDao
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.repository.UserDataRepository
import com.jmblfma.wheely.utils.UserSessionManager
import kotlinx.coroutines.launch

class UserDataViewModel : ViewModel() {
    private val userDao: UserDao = RoomDatabaseBuilder.sharedInstance.userDao()
    private val repository = UserDataRepository.sharedInstance
    private val _fetchedUser = MutableLiveData<User?>()
    private val _userUpdateStatus = MutableLiveData<String?>()
    private val _userAdditionStatus = MutableLiveData<String?>()
    val userPostStatus: LiveData<String?>
        get() = _userAdditionStatus
    val userUpdateStatus: LiveData<String?>
        get() = _userUpdateStatus
    val fetchedUser: LiveData<User?>
        get() = _fetchedUser

    fun fetchUser(userEmail: String){
        viewModelScope.launch {
           _fetchedUser.postValue(repository.getUserByEmail(userEmail))
        }
    }

    fun deleteUser(userEmail: String){
        viewModelScope.launch {
            repository.deleteUser(userEmail)
        }
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            repository.addUser(user) { isSuccess ->
                if (isSuccess) {
                    _userAdditionStatus.postValue("User added successfully")
                } else {
                    _userAdditionStatus.postValue("User with this email already exists")
                }
            }
        }
    }

    fun updateUserBanner(userId: Int, bannerPath: String) {
        viewModelScope.launch {
            repository.updateUserBanner(userId, bannerPath) { rowsAffected ->
                if (rowsAffected == 1) {
                    _userUpdateStatus.postValue("${UserSessionManager.getCurrentUser()?.nickname} has changed the data successfully")
                } else {
                    _userUpdateStatus.postValue("The data hasn't been changed due to an error")
                }
            }
        }
    }

    fun updateUserPersonalInfo(
        userId: Int,
        newNickname: String?,
        newFirstName: String?,
        newLastName: String?,
        newDateOfBirth: String?,
        newProfileImage: String?
    ) {
        viewModelScope.launch {
            repository.updateUserPersonalInfo(
                userId,
                newNickname,
                newFirstName,
                newLastName,
                newDateOfBirth,
                newProfileImage
            ) { rowsAffected ->
                if (rowsAffected == 1) {
                    _userUpdateStatus.postValue("${UserSessionManager.getCurrentUser()?.nickname} has changed the data successfully")
                } else {
                    _userUpdateStatus.postValue("The data hasn't been changed due to an error")
                }
            }
        }
    }
}