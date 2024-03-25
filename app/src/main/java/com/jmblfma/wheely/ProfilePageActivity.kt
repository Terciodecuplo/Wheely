package com.jmblfma.wheely

import android.os.Bundle
import com.jmblfma.wheely.adapter.TrackHistoryAdapter
import com.jmblfma.wheely.databinding.UserProfileMainBinding
import com.jmblfma.wheely.model.Post
import com.jmblfma.wheely.utils.NavigationMenuActivity
import java.util.ArrayList

class ProfilePageActivity : NavigationMenuActivity() {
    private lateinit var binding: UserProfileMainBinding
    private lateinit var trackHistoryAdapter: TrackHistoryAdapter
    private lateinit var trackHistoryList: ArrayList<Post>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserProfileMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()

        trackHistoryList = ArrayList()

    }

    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_profile
    }
}