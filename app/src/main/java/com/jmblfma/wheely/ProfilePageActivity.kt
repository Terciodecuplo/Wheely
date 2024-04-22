package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jmblfma.wheely.adapter.ProfileViewPagerAdapter
import com.jmblfma.wheely.databinding.UserProfileMainBinding
import com.jmblfma.wheely.model.DataSummary
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.utils.UserLoginState
import com.jmblfma.wheely.viewmodels.AddVehicleViewModel
import java.time.LocalDate
import java.time.ZonedDateTime

class ProfilePageActivity : NavigationMenuActivity() {
    private lateinit var binding: UserProfileMainBinding
    private lateinit var trackHistoryList: ArrayList<Track>
    private val viewModel: AddVehicleViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserProfileMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()
        setSupportActionBar(binding.toolbarProfile)

        val viewPager: ViewPager2 = binding.viewPager
        val tabLayout: TabLayout = binding.tabLayout

        trackHistoryList = ArrayList()
        trackHistoryList.add(exampleData())
        trackHistoryList.add(exampleData())
        trackHistoryList.add(exampleData())
        trackHistoryList.add(exampleData())
        trackHistoryList.add(exampleData())

        val profileViewPagerAdapter = ProfileViewPagerAdapter(this, trackHistoryList)

        binding.viewPager.adapter = profileViewPagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "History"
                1 -> "Vehicles"
                else -> null
            }
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout_menu_option -> {
                val userLoginState = UserLoginState(this)
                userLoginState.isLoggedIn = false
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

            R.id.add_vehicle_menu_option -> {
                val intent = Intent(applicationContext, AddVehicleActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_profile
    }

    fun exampleData(): Track {
        val user = User()

        val vehicle = Vehicle(
            vehicleId = 1,
            ownerId = user.userId,
            name = "Triciclo",
            brand = "Yamaha",
            model = "MT-07",
            year = "2017",
            horsepower = 500,
            dateAdded = LocalDate.now()
        )

        val trackData = arrayListOf<TrackPoint>()
        val dataSummary = DataSummary(
            summaryId = 1,
            elapsedTime = 3600.0,
            maxSpeed = 150.0,
            averageSpeed = 100.0,
            distanceTraveled = 323.0,
            maxInclination = 10.0,
            averageInclination = 5.0,
            maxAltitude = 200.0,
            deltaAltitude = 50.0
        )

        val track = Track(
            trackId = 1,
            drivenBy = user,
            vehicleUsed = vehicle,
            name = "Morning Route around Elche",
            generalLocation = "Elche",
            creationDate = ZonedDateTime.now(),
            trackData = trackData,
            trackDifficulty = "Medium",
            trackSummary = dataSummary
        )

        return track;
    }
}
