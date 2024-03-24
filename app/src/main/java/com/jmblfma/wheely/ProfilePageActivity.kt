package com.jmblfma.wheely

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jmblfma.wheely.databinding.UserProfileMainBinding

class ProfilePageActivity : AppCompatActivity() {
    private lateinit var binding: UserProfileMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserProfileMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}