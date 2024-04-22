package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jmblfma.wheely.databinding.ActivityMainBinding
import com.jmblfma.wheely.utils.UserLoginState

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userLoginState = UserLoginState(this)

        if (userLoginState.isFirstLaunch) {
            userLoginState.isFirstLaunch = false
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } else if( !userLoginState.isFirstLaunch && !userLoginState.isLoggedIn){
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } else if(userLoginState.isLoggedIn){
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
    }
}