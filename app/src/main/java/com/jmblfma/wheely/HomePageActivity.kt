package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.jmblfma.wheely.utils.UserSessionManager
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
                UserSessionManager.logoutUser()
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

}