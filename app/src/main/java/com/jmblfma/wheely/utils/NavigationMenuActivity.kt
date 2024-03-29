package com.jmblfma.wheely.utils

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jmblfma.wheely.HomePageActivity
import com.jmblfma.wheely.ProfilePageActivity
import com.jmblfma.wheely.R
import com.jmblfma.wheely.RecordActivityTest

abstract class NavigationMenuActivity : AppCompatActivity() {
    protected fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_posts -> {
                    startActivityIfNeeded(Intent(this, HomePageActivity::class.java), 0)
                    true
                }

                R.id.nav_record -> {
                    startActivityIfNeeded(Intent(this, RecordActivityTest::class.java), 0)
                    true
                }

                R.id.nav_profile -> {
                    startActivityIfNeeded(Intent(this, ProfilePageActivity::class.java), 0)
                    true
                }

                else -> false
            }
        }


        val menu = bottomNavigation.menu
        val menuItemId = getBottomNavigationMenuItemId()
        val menuItem = menu.findItem(menuItemId)
        menuItem.isChecked = true
    }

    // Force those subclasses that implements this method, to specify which menu item should be highlighted
    abstract fun getBottomNavigationMenuItemId(): Int
}
