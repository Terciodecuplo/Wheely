package com.jmblfma.wheely

import android.os.Bundle
import com.jmblfma.wheely.databinding.UserProfileMainBinding
import com.jmblfma.wheely.utils.NavigationMenuActivity

class ProfilePageActivity : NavigationMenuActivity() {
    private lateinit var binding: UserProfileMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserProfileMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()
    }

    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_profile
    }
}