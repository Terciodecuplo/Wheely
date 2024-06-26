package com.jmblfma.wheely.utils

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jmblfma.wheely.HomePageActivity
import com.jmblfma.wheely.ProfilePageActivity
import com.jmblfma.wheely.R
import com.jmblfma.wheely.TrackRecordingActivity
import com.jmblfma.wheely.TrackViewerActivity

abstract class NavigationMenuActivity : AppCompatActivity() {
    protected fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_posts -> {
                        startActivityIfNeeded(Intent(this, HomePageActivity::class.java), 0)
                        finish()
                        true
                }
                R.id.nav_record -> {
                    startActivityIfNeeded(Intent(this, TrackRecordingActivity::class.java), 0)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivityIfNeeded(Intent(this, ProfilePageActivity::class.java), 0)
                    finish()
                    true
                }

                R.id.nav_viewer -> {
                    startActivityIfNeeded(Intent(this, TrackViewerActivity::class.java), 0)
                    finish()
                    true
                }
                else -> false
            }
        }
        bottomNavigation.setOnItemReselectedListener {}
        val menu = bottomNavigation.menu
        val menuItemId = getBottomNavigationMenuItemId()
        val menuItem = menu.findItem(menuItemId)
        menuItem.isChecked = true
    }

    // Force those subclasses that implements this method, to specify which menu item should be highlighted
    abstract fun getBottomNavigationMenuItemId(): Int
}
