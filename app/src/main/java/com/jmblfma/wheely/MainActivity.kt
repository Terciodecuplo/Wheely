package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.jmblfma.wheely.databinding.ActivityMainBinding
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.repository.UserDataRepository
import com.jmblfma.wheely.utils.LoginStateManager
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.UserDataViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val repository = UserDataRepository.sharedInstance
    private val viewModel: UserDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.userAdditionStatus.observe(this) { status ->
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
            attemptToLogin(email)

        }

        binding.signupButton.setOnClickListener {
            val user = User(0)
            viewModel.addUser(user)
        }
    }

    private fun showLoginScreen() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun attemptToLogin(email: String) {
        lifecycleScope.launch {
            repository.getUserByEmail(email) { user ->
                if (user != null) {
                    UserSessionManager.loginUser(user)
                    navigateToHomePage()
                } else {
                    showSnackbar("The user doesn't exist")
                }
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