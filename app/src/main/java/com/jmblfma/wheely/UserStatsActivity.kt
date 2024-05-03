package com.jmblfma.wheely

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jmblfma.wheely.databinding.UserStatsLayoutBinding
import com.jmblfma.wheely.utils.UserSessionManager

class UserStatsActivity : AppCompatActivity() {
    private lateinit var binding: UserStatsLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserStatsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarUserStats)
        getUserData()
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


}