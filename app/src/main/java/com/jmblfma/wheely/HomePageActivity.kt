package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.jmblfma.wheely.adapter.PostsAdapter
import com.jmblfma.wheely.databinding.HomePageBinding
import com.jmblfma.wheely.utils.LanguageSelector
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.HomePageViewModel

class HomePageActivity : NavigationMenuActivity() {
    private lateinit var binding: HomePageBinding
    private val viewModel: HomePageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        LanguageSelector.updateLocale(this, LanguageSelector.loadLanguage(this))
        setSupportActionBar(binding.toolbarHome)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTitle.text = getString(R.string.post_layout_title)

        setupFeedObservers()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchTrackList()
        viewModel.fetchUserList()
    }

    private fun setupFeedObservers() {
        viewModel.combinedData.observe(this) { (trackList, userList) ->
            if (trackList != null && userList != null) {
                val usersById = userList.associateBy { it.userId }
                binding.postRecycler.adapter = PostsAdapter(trackList, usersById)
                binding.postRecycler.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
                binding.postRecycler.scrollToPosition(trackList.size - 1)
            }
        }
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
                startActivity(Intent(this, HomePageActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }
        return true
    }

    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_posts // Return the ID of the bottom navigation menu item for HomePageActivity
    }
}

