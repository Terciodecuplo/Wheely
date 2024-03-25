package com.jmblfma.wheely

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jmblfma.wheely.adapter.PostsAdapter
import com.jmblfma.wheely.databinding.HomePageBinding
import com.jmblfma.wheely.model.Post
import com.jmblfma.wheely.utils.NavigationMenuActivity

class HomePageActivity : NavigationMenuActivity() {
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
        setupBottomNavigation()
    }

    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_posts // Return the ID of the bottom navigation menu item for HomePageActivity
    }

}