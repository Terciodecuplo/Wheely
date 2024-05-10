package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.jmblfma.wheely.adapter.PostsAdapter
import com.jmblfma.wheely.databinding.HomePageBinding
import com.jmblfma.wheely.model.Post
import com.jmblfma.wheely.utils.LanguageSelector
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.utils.UserSessionManager

class HomePageActivity : NavigationMenuActivity() {
    private lateinit var binding: HomePageBinding
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var postList: ArrayList<Post>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarHome)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTitle.text = getString(R.string.post_layout_title)
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
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

            R.id.change_lang_menu_option -> {
                val newLang = if (LanguageSelector.getCurrentLanguage() == "en") "es" else "en"
                LanguageSelector.saveLanguage(this, newLang)
                LanguageSelector.updateLocale(this, newLang)
                recreate() // Used to refresh te activity
            }
        }
        return true
    }

    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_posts // Return the ID of the bottom navigation menu item for HomePageActivity
    }
}