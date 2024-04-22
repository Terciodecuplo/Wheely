package com.jmblfma.wheely

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.jmblfma.wheely.adapter.PostsAdapter
import com.jmblfma.wheely.databinding.HomePageBinding
import com.jmblfma.wheely.model.DataSummary
import com.jmblfma.wheely.model.Post
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.utils.UserLoginState
import java.time.LocalDate
import java.time.ZonedDateTime

class HomePageActivity : NavigationMenuActivity() {
    private lateinit var binding: HomePageBinding
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var postList: ArrayList<Post>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarHome)


        postList = ArrayList()
        postList.add(exampleData())
        postList.add(exampleData())
        postList.add(exampleData())
        postList.add(exampleData())
        postList.add(exampleData())

        postsAdapter = PostsAdapter(postList, this)
        binding.postRecycler.adapter = postsAdapter
        binding.postRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        setupBottomNavigation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_home_menu, menu)
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

        }
        return true
    }

    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_posts // Return the ID of the bottom navigation menu item for HomePageActivity
    }

    // This is just an example Data. It will be removed in the future.
    fun exampleData(): Post {
        val user = User(0)
        val vehicle = Vehicle(
            vehicleId = 1,
            ownerId = user.userId,
            name = "Triciclo",
            brand = "Yamaha",
            model = "Mt07",
            year = "2017",
            horsepower = 500,
            dateAdded = LocalDate.now().toString()
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
            userId = 0,
            vehicleId = 1,
            name = "Morning Route around Elche",
            generalLocation = "Elche",
            creationTimestamp = ZonedDateTime.now().toString(),
            difficulty = "Medium",
        )

        val post = Post(
            postId = 1,
            description = "Great morning route around Elche!",
            postedBy = user,
            associatedTrack = track,
            datePublished = ZonedDateTime.parse("2024-01-02T08:00:00+01:00[Europe/Madrid]")
        )
        return post;
    }

}