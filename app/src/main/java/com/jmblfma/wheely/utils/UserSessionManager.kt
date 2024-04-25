package com.jmblfma.wheely.utils

import com.jmblfma.wheely.model.User

object UserSessionManager {
    private var currentUser: User? = null

    fun loginUser(user: User) {
        currentUser = user
        LoginStateManager.setLoggedIn(true)
    }

    fun logoutUser() {
        currentUser = null
        LoginStateManager.setLoggedIn(false)
    }

    fun getCurrentUser(): User? = currentUser
    fun isLoggedIn(): Boolean {
        return LoginStateManager.isLoggedIn()
    }
}
