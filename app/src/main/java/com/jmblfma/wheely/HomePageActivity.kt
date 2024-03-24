package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.jmblfma.wheely.adapter.PostsAdapter
import com.jmblfma.wheely.databinding.HomePageBinding
import com.jmblfma.wheely.model.Post

class HomePageActivity : AppCompatActivity() {
    private lateinit var binding: HomePageBinding
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var postList: ArrayList<Post>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postList = ArrayList()

        postList.add(Post(1, "Jose", "Murcia", "Elche", "Morning Route around Elche", "01/02/2024"))
        postList.add(Post(2, "Pepe", "Murcia", "Elche", "Morning Route around Elche", "01/02/2024"))
        postList.add(Post(3, "Carol", "Muñoz", "Elche", "Morning Route around Elche", "01/02/2024"))
        postList.add(Post(4, "Luis", "Martinez", "Daya Nueva", "LiLu Route", "23/01/2024"))
        postList.add(Post(5, "Lia", "Ferraro", "Daya Nueva", "Lilu Route", "23/01/2024"))
        postList.add(Post(6, "Domi", "Martinez", "Albatera", "Riding to Padel", "02/02/2024"))

        postsAdapter = PostsAdapter(postList, this)
        binding.postRecycler.adapter = postsAdapter
        binding.postRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val bottomNavigationMenu = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_posts -> {
                    startActivity((Intent(this, HomePageActivity::class.java)))
                    true
                }

                R.id.nav_record -> {
                    Snackbar.make(bottomNavigationMenu, "Not available yet", Snackbar.LENGTH_SHORT)
                        .show()
                    true
                }

                R.id.nav_profile -> {
                    startActivity((Intent(this, ProfilePageActivity::class.java)))
                    true
                }

                else -> false
            }
        }


    }

    private fun openProfileScreen() {
        val intent = Intent(this, ProfilePageActivity::class.java)
        startActivity(intent)
    }


}