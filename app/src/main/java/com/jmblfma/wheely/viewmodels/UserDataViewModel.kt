package com.jmblfma.wheely.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.repository.UserDataRepository
import com.jmblfma.wheely.utils.UserSessionManager
import kotlinx.coroutines.launch

class UserDataViewModel : ViewModel() {
    private val repository = UserDataRepository.sharedInstance


    private val _fetchedUser = MutableLiveData<User?>()
    val fetchedUser: LiveData<User?> = _fetchedUser
    private val _userUpdateStatus = MutableLiveData<String?>()
    val userUpdateStatus: LiveData<String?> = _userUpdateStatus
    private val _fetchAllUsers = MutableLiveData<List<User>>()
    val fetchAllUsers: LiveData<List<User>> = _fetchAllUsers

    fun setUserCandidate(newUser: User) {
        viewModelScope.launch {
            repository.setUserCandidate(newUser)
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            _fetchAllUsers.postValue(repository.fetchUsers())
        }
    }

    fun fetchUser(userEmail: String) {
        viewModelScope.launch {
            _fetchedUser.postValue(repository.getUserByEmail(userEmail))
        }
    }

    fun deleteUser(userEmail: String) {
        viewModelScope.launch {
            repository.deleteUser(userEmail)
        }
    }

    fun updateUserBanner(userId: Int, bannerPath: String?) {
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