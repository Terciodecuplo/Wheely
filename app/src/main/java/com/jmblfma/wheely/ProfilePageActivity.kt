package com.jmblfma.wheely

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jmblfma.wheely.adapter.ProfileViewPagerAdapter
import com.jmblfma.wheely.databinding.UserProfileMainBinding
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.utils.ImagePicker
import com.jmblfma.wheely.utils.LoginStateManager
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.utils.PermissionsManager
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.UserDataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ProfilePageActivity : NavigationMenuActivity() {
    private lateinit var binding: UserProfileMainBinding
    private lateinit var trackHistoryList: ArrayList<Track>
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private val viewModel: UserDataViewModel by viewModels()
    private var photoURI: Uri? = null
    private var savedPath: String? = null

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserProfileMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()

        setSupportActionBar(binding.toolbarProfile)
        setupImagePickerLauncher()
        setupTakePictureLauncher()

        val viewPager: ViewPager2 = binding.viewPager
        val tabLayout: TabLayout = binding.tabLayout
        binding.editBannerProfile.setOnClickListener {
            showImageSourceDialog()
        }
        profileUserMainDataSetup()
        trackHistoryList = ArrayList()

        val profileViewPagerAdapter = ProfileViewPagerAdapter(this, trackHistoryList)

        binding.viewPager.adapter = profileViewPagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "History"
                1 -> "Vehicles"
                else -> null
            }
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout_menu_option -> {
                LoginStateManager.setLoggedIn(false)
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

            R.id.add_vehicle_menu_option -> {
                val intent = Intent(applicationContext, AddVehicleActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }


    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_profile
    }


    private fun profileUserMainDataSetup() {
        Log.d("TESTING","ProfilePageActivity/ binding.userName.text ${binding.userName.text}")

        binding.userName.text =
            UserSessionManager.getCurrentUser()?.nickname ?: "[no_user_selected]"
        setProfileImage(binding.profileImage, UserSessionManager.getCurrentUser()?.profileImage)
        if (UserSessionManager.getCurrentUser()?.profileBanner.isNullOrEmpty()) {
            binding.bannerProfile.setImageResource(R.drawable.ic_banner_placeholder)
        } else {
            setBannerImage(
                binding.bannerProfile,
                UserSessionManager.getCurrentUser()?.profileBanner
            )
        }
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

    private fun setBannerImage(imageView: ImageView, imagePath: String?) {
        if (!imagePath!!.startsWith("/")) {
            Glide.with(imageView.context)
                .load(R.drawable.ic_banner_placeholder) // Your placeholder drawable
                .into(imageView)
        } else {
            Glide.with(imageView.context)
                .load(imagePath)
                .into(imageView)
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> chooseImageFromGallery()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun setupImagePickerLauncher() {
        val currentBannerImagePath = UserSessionManager.getCurrentUser()?.profileBanner
        val imageId = UUID.randomUUID().toString()
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let { receivedUri ->
                    // Use Glide to load and display the image without delays
                    Glide
                        .with(this@ProfilePageActivity)
                        .load(receivedUri)
                        .into(binding.bannerProfile)
                    val bitmap = ImagePicker.fixImageOrientation(this, uri)
                    // Save the image asynchronously
                    lifecycleScope.launch(Dispatchers.IO) {
                        bitmap?.let { it ->
                            currentBannerImagePath?.let { path ->
                                val file = File(path)
                                if (file.exists()) {
                                    file.delete()
                                }
                            }
                            savedPath = ImagePicker.saveImageToInternalStorage(
                                this@ProfilePageActivity, it, "banner-pic-$imageId.jpg"
                            )
                            runOnUiThread { // This waits until the I/O saving operation finishes to persist the path into the DB
                                updateUserBanner()
                            }
                        }
                    }
                }
            }
    }

    private fun setupTakePictureLauncher() {
        val currentBannerImagePath = UserSessionManager.getCurrentUser()?.profileBanner
        val imageId = UUID.randomUUID().toString()
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    photoURI?.let { receivedUri ->
                        binding.bannerProfile.setImageURI(receivedUri)
                        val bitmap = ImagePicker.fixImageOrientation(this, receivedUri)
                        lifecycleScope.launch(Dispatchers.IO) {
                            bitmap?.let { it ->
                                currentBannerImagePath?.let { path ->
                                    val file = File(path)
                                    if (file.exists()) {
                                        file.delete()
                                    }
                                }
                                savedPath = ImagePicker.saveImageToInternalStorage(
                                    this@ProfilePageActivity, it, "banner-pic-$imageId.jpg"
                                )
                                withContext(Dispatchers.Main) { // This waits until the I/O saving operation finishes to persist the path into the DB
                                    updateUserBanner()
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            takePicture()
        } else {
            PermissionsManager.getCameraPermission(this, CAMERA_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                takePicture()
            } else {
                // Permission is denied
                showSnackbar("Camera permission is necessary to use the camera")
            }
        }
    }

    private fun takePicture() {
        // Ensure the device has a camera
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            photoURI =
                ImagePicker.createImageFile(this)
            takePictureLauncher.launch(photoURI)
        } else {
            showSnackbar("This device does not have a camera")
        }
    }

    private fun chooseImageFromGallery() {
        // MIME type for image/* to select any image type
        imagePickerLauncher.launch("image/*")
    }

    private fun updateUserBanner() {
        Log.d("UPDATE USER", "Banner Path: ${savedPath.toString()}")
        UserSessionManager.getCurrentUser()?.userId?.let {
            viewModel.updateUserBanner(
                it,
                savedPath.toString()
            )
        }
        val updatedUser = UserSessionManager.getCurrentUser()?.let {
            User(
                it.userId,
                it.nickname,
                it.firstName,
                it.lastName,
                it.email,
                it.dateOfBirth,
                it.profileImage,
                savedPath.toString()
            )
        }
        UserSessionManager.updateLoggedUser(updatedUser)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.new_user_layout), message, Snackbar.LENGTH_LONG).show()
    }
}