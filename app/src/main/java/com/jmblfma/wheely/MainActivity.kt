package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.jmblfma.wheely.databinding.ActivityMainBinding
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.utils.UserLoginState
import com.jmblfma.wheely.viewmodels.UserDataViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.userAdditionStatus.observe(this) { status ->
            status?.let {
                showSnackbar(it)
            }
        }
        val userLoginState = UserLoginState(this)

        if (userLoginState.isFirstLaunch) {
            userLoginState.isFirstLaunch = false
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } else if (!userLoginState.isFirstLaunch && !userLoginState.isLoggedIn) {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } else if (userLoginState.isLoggedIn) {
            val intent = Intent(applicationContext, HomePageActivity::class.java)
            this.startActivity(intent)
            finish()
        }

        binding.loginButton.setOnClickListener {
            userLoginState.isLoggedIn = true
            val intent = Intent(applicationContext, HomePageActivity::class.java)
            this.startActivity(intent)
            finish()
        }

        binding.signupButton.setOnClickListener {
            val user = User(0)
            viewModel.addUser(user)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.main_layout), message, Snackbar.LENGTH_LONG).show()
    }
}