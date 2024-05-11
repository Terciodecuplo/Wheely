package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.jmblfma.wheely.databinding.ActivityMainBinding
import com.jmblfma.wheely.utils.LanguageSelector
import com.jmblfma.wheely.utils.LoginStateManager
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.UserDataViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageSelector.updateLocale(this, LanguageSelector.loadLanguage(this))
        binding = ActivityMainBinding.inflate(layoutInflater)
        if (LoginStateManager.isFirstLaunch) {
            LoginStateManager.isFirstLaunch = false
            showLoginScreen()
        } else if (!UserSessionManager.isLoggedIn()) {
            showLoginScreen()
        } else {
            viewModel.fetchAllUsers()
            checkUsersInDB()
        }
        binding.languageButton.setOnClickListener {
            toggleLanguage()
        }
        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            Log.d("TESTING", "MainActivity/ loginButton $email")
            attemptToLogin(email)

        }

        binding.signupButton.setOnClickListener {
            val intent = Intent(this, NewUserActivity::class.java)
            this.startActivity(intent)
        }
    }

    private fun checkUsersInDB() {
        viewModel.fetchAllUsers.observe(this) {
            if (it.isEmpty()) {
                UserSessionManager.logoutUser()
                showLoginScreen()
            } else {
                navigateToHomePage()
            }
        }
    }

    private fun toggleLanguage() {
        val newLang = if (LanguageSelector.getCurrentLanguage() == "en") "es" else "en"

        LanguageSelector.saveLanguage(this, newLang)
        LanguageSelector.updateLocale(this, newLang)
        //recreate() // Used to refresh te activity

        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
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