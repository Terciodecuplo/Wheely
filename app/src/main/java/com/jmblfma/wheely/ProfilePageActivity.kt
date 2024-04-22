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
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.utils.UserLoginState
import com.jmblfma.wheely.viewmodels.NewVehicleDataViewModel
import java.time.LocalDate
import java.time.ZonedDateTime

class ProfilePageActivity : NavigationMenuActivity() {
    private lateinit var binding: UserProfileMainBinding
    private lateinit var trackHistoryList: ArrayList<Track>
    private val viewModel: NewVehicleDataViewModel by viewModels()
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


        val user = User(
            userId = 1,
            nickname = "MoToreto",
            firstName = "Jose",
            lastName = "Murcia",
            email = "jose@example.com",
            dateOfBirth = "1990-01-01",
        )

        val vehicle = Vehicle(
            vehicleId = 1,
            ownerId = user.userId,
            name = "Triciclo",
            brand = "Yamaha",
            model = "MT-07",
            year = "2017",
            horsepower = 500,
            dateAdded = LocalDate.now().toString()
        )



        val trackData = arrayListOf<TrackPoint>()

        val track = Track(
            trackId = 1,
            userId = 1,
            vehicleId = 1,
            name = "Morning Route around Elche",
            generalLocation = "Elche",
            creationTimestamp = ZonedDateTime.now().toString(),
            //trackData = trackData,
            difficulty = "Medium",
        )

        return track;
    }

    fun exampleVehicle(): Vehicle {

        val user = User(
            userId = 1,
            nickname = "MoToreto",
            firstName = "Jose",
            lastName = "Murcia",
            email = "jose@example.com",
            dateOfBirth = "1990-01-01",

        )

        val vehicle = Vehicle(
            vehicleId = 1,
            ownerId = 1,
            name = "Triciclo",
            brand = "Yamaha",
            model = "MT-07",
            year = "2017",
            horsepower = 500,
            dateAdded = LocalDate.now().toString()
        )
        return vehicle
    }
}
