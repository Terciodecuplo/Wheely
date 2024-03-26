package com.jmblfma.wheely

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.jmblfma.wheely.adapter.PostsAdapter
import com.jmblfma.wheely.adapter.TrackHistoryAdapter
import com.jmblfma.wheely.databinding.UserProfileMainBinding
import com.jmblfma.wheely.model.DataSummary
import com.jmblfma.wheely.model.PointData
import com.jmblfma.wheely.model.Post
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.utils.NavigationMenuActivity
import java.time.ZonedDateTime

class ProfilePageActivity : NavigationMenuActivity() {
    private lateinit var binding: UserProfileMainBinding
    private lateinit var trackHistoryAdapter: TrackHistoryAdapter
    private lateinit var trackHistoryList: ArrayList<Track>


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserProfileMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()

        trackHistoryList = ArrayList()
        trackHistoryList.add(exampleData())
        trackHistoryList.add(exampleData())
        trackHistoryList.add(exampleData())
        trackHistoryList.add(exampleData())
        trackHistoryList.add(exampleData())

        trackHistoryAdapter = TrackHistoryAdapter(trackHistoryList, this)
        binding.trackHistoryRecycler.adapter= trackHistoryAdapter
        binding.trackHistoryRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        setupBottomNavigation()
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
            model = "Mt07",
            year = "2017",
            horsepower = 500,
            dateAdded = ZonedDateTime.now()
        )

        user.ownedVehicles.add(vehicle)

        val trackData = arrayListOf<PointData>()
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
}