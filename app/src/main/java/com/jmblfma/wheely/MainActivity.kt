package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.jmblfma.wheely.databinding.ActivityMainBinding
import com.jmblfma.wheely.repository.UserDataRepository
import com.jmblfma.wheely.utils.LoginStateManager
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.UserDataViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val repository = UserDataRepository.sharedInstance
    private val viewModel: UserDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkUsersInDB()
        viewModel.userPostStatus.observe(this) { status ->
            status?.let {
                showSnackbar(it)
            }
        }
        if (LoginStateManager.isFirstLaunch) {
            LoginStateManager.isFirstLaunch = false
            showLoginScreen()
        } else if (!UserSessionManager.isLoggedIn()) {
            showLoginScreen()
        } else {
            navigateToHomePage()
        }

        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            Log.d("TESTING", "MainActivity/ loginButton ${email}")
            attemptToLogin(email)

        }

        binding.signupButton.setOnClickListener {
            val intent = Intent(applicationContext, NewUserActivity::class.java)
            this.startActivity(intent)
        }
    }

    private fun checkUsersInDB(){
        viewModel.fetchAllUsers.observe(this){
            if(it.isEmpty()){
                UserSessionManager.logoutUser()
            }
        }
    }

    private fun showLoginScreen() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun attemptToLogin(email: String) {
        viewModel.fetchUser(email)
        viewModel.fetchedUser.observe(this) {
            Log.d("TESTING", "MainActivity/ attemptToLogin() executed")
            if (it != null) {
                UserSessionManager.loginUser(it)
                navigateToHomePage()
                finish()
            } else {
                Log.d("TESTING", "MainActivity/ USER DOES NOT EXIST")
                showSnackbar(getString(R.string.missing_user))

            }
        }
    }

    private fun navigateToHomePage() {
        val intent = Intent(applicationContext, HomePageActivity::class.java)
        this.startActivity(intent)
        finish()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.main_layout), message, Snackbar.LENGTH_LONG).show()
    }
}