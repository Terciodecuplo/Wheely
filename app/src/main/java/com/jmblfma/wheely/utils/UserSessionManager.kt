package com.jmblfma.wheely.utils

import com.jmblfma.wheely.model.User

object UserSessionManager {

    fun loginUser(user: User?) {
        LoginStateManager.saveCurrentUser(user)
        LoginStateManager.setLoggedIn(true)
    }

    fun logoutUser() {
        LoginStateManager.saveCurrentUser(null)
        LoginStateManager.setLoggedIn(false)
    }

    fun updateLoggedUser(user: User?) {
        LoginStateManager.saveCurrentUser(user)
    }

    fun getCurrentUser(): User? = LoginStateManager.getCurrentUser()
    fun isLoggedIn(): Boolean = LoginStateManager.isLoggedIn()
}