package com.jmblfma.wheely

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
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
import java.time.ZonedDateTime

class ProfilePageActivity : NavigationMenuActivity() {
    private lateinit var binding: UserProfileMainBinding
    private lateinit var trackHistoryList: ArrayList<Track>
    private lateinit var vehicleList: ArrayList<Vehicle>


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserProfileMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()
        setSupportActionBar(binding.toolbar)

        val viewPager: ViewPager2 = binding.viewPager
        val tabLayout: TabLayout = binding.tabLayout

        trackHistoryList = ArrayList()
        trackHistoryList.add(exampleData())
        trackHistoryList.add(exampleData())
        trackHistoryList.add(exampleData())
        trackHistoryList.add(exampleData())
        trackHistoryList.add(exampleData())

        vehicleList = ArrayList()
        vehicleList.add(exampleVehicle())
        vehicleList.add(exampleVehicle())
        vehicleList.add(exampleVehicle())
        vehicleList.add(exampleVehicle())

        val profileViewPagerAdapter = ProfileViewPagerAdapter(this, trackHistoryList, vehicleList)

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
        when(item.itemId){
            R.id.logout -> {
                val userLoginState = UserLoginState(this)
                userLoginState.isLoggedIn = false
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        return true
    }
    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_profile
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun exampleData(): Track {
        val user = User(
            userId = 1,
            name = "MoToreto",
            firstName = "Jose",
            lastName = "Murcia",
            email = "jose@example.com",
            dateOfBirth = "1990-01-01",
            drivenTracks = arrayListOf(),
            ownedVehicles = arrayListOf()
        )

        val vehicle = Vehicle(
            vehicleId = 1,
            owner = user,
            name = "Triciclo",
            brand = "Yamaha",
            model = "MT-07",
            year = "2017",
            horsepower = 500,
            dateAdded = ZonedDateTime.now()
        )

        user.ownedVehicles.add(vehicle)

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

        user.drivenTracks.add(track)

        return track;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun exampleVehicle(): Vehicle {

        val user = User(
            userId = 1,
            name = "MoToreto",
            firstName = "Jose",
            lastName = "Murcia",
            email = "jose@example.com",
            dateOfBirth = "1990-01-01",
            drivenTracks = arrayListOf(),
            ownedVehicles = arrayListOf()
        )

        val vehicle = Vehicle(
            vehicleId = 1,
            owner = user,
            name = "Triciclo",
            brand = "Yamaha",
            model = "MT-07",
            year = "2017",
            horsepower = 500,
            dateAdded = ZonedDateTime.now()
        )
        return vehicle
    }
}
