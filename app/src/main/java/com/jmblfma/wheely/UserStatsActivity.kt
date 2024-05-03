package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.jmblfma.wheely.databinding.UserStatsLayoutBinding
import com.jmblfma.wheely.utils.LoginStateManager
import com.jmblfma.wheely.utils.UserSessionManager

class UserStatsActivity : AppCompatActivity() {
    private lateinit var binding: UserStatsLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserStatsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarUserStats)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTitle.text = getString(R.string.stats_layout_title)
        getUserData()
        //val editImage =
        binding.submitChangesButton.setOnClickListener {
            submitChanges()
        }

    }

    private fun submitChanges() {
        binding.userNicknameEdittext.isClickable = false
        binding.userNicknameEdittext.isFocusable = false
        binding.userNicknameEdittext.isFocusableInTouchMode = false
        binding.userFirstnameEdittext.isClickable = false
        binding.userFirstnameEdittext.isFocusable = false
        binding.userFirstnameEdittext.isFocusableInTouchMode = false
        binding.userLastnameEdittext.isClickable = false
        binding.userLastnameEdittext.isFocusable = false
        binding.userLastnameEdittext.isFocusableInTouchMode = false
        binding.userBirthdayEdittext.isClickable = false
        binding.submitChangesButton.visibility = View.INVISIBLE
        Toast.makeText(this, "User data submitted!", Toast.LENGTH_SHORT).show()
    }

    private fun setProfileImage(imageView: ImageView, imagePath: String?) {
        if (!imagePath!!.startsWith("/")) {
            Glide.with(imageView.context)
                .load(R.drawable.user_default_pic) // Your placeholder drawable
                .into(imageView)
        } else {
            Glide.with(imageView.context)
                .load(imagePath)
                .into(imageView)
        }
    }

    private fun getUserData(){
        setProfileImage(binding.userImage, UserSessionManager.getCurrentUser()?.profileImage)
        binding.userNicknameEdittext.setText(UserSessionManager.getCurrentUser()?.nickname)
        binding.userFirstnameEdittext.setText(UserSessionManager.getCurrentUser()?.firstName)
        binding.userLastnameEdittext.setText(UserSessionManager.getCurrentUser()?.lastName)
        binding.userEmailEdittext.setText(UserSessionManager.getCurrentUser()?.email)
        binding.userBirthdayEdittext.setText(UserSessionManager.getCurrentUser()?.dateOfBirth)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_stats_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_user_menu_option -> {
                editUserData()
            }

            R.id.remove_user_menu_option -> {

            }
        }
        return true
    }

    private fun editUserData() {
        binding.userNicknameEdittext.isClickable = true
        binding.userNicknameEdittext.isFocusable = true
        binding.userNicknameEdittext.isFocusableInTouchMode = true
        binding.userFirstnameEdittext.isClickable = true
        binding.userFirstnameEdittext.isFocusable = true
        binding.userFirstnameEdittext.isFocusableInTouchMode = true
        binding.userLastnameEdittext.isClickable = true
        binding.userLastnameEdittext.isFocusable = true
        binding.userLastnameEdittext.isFocusableInTouchMode = true
        binding.userBirthdayEdittext.isClickable = true
        binding.submitChangesButton.visibility = View.VISIBLE
    }


}