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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jmblfma.wheely.adapter.ProfileViewPagerAdapter
import com.jmblfma.wheely.databinding.UserProfileMainBinding
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.utils.ImagePicker
import com.jmblfma.wheely.utils.LanguageSelector
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.utils.PermissionsManager
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.UserDataViewModel
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
        LanguageSelector.updateLocale(this, LanguageSelector.loadLanguage(this))
        setupBottomNavigation()
        setupToolbar()
        setupImagePickerLauncher()
        setupTakePictureLauncher()
        val viewPager: ViewPager2 = binding.viewPager
        val tabLayout: TabLayout = binding.tabLayout
        profileUserMainDataSetup()
        trackHistoryList = ArrayList()

        val profileViewPagerAdapter = ProfileViewPagerAdapter(this, trackHistoryList)

        binding.viewPager.adapter = profileViewPagerAdapter
        binding.profileImage.setOnClickListener {
            val intent = Intent(this, UserStatsActivity::class.java)
            startActivity(intent)
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.history_option)
                1 -> getString(R.string.vehicles_option)
                else -> null
            }
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        profileUserMainDataSetup()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Check if HomeActivity is in the back stack
        val startMain = Intent(this, HomePageActivity::class.java)
        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(startMain)
        finish()
    }

    private fun setupToolbar(){
        setSupportActionBar(binding.toolbarProfile)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTitle.text = getString(R.string.profile_layout_title)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_profile_menu, menu)
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

            R.id.edit_highlights_menu_option -> {
                showHighlightsDialog()
            }

            R.id.edit_banner_menu_option -> {
                showImageSourceDialog()
            }

            R.id.add_vehicle_menu_option -> {
                val intent = Intent(this, AddVehicleActivity::class.java)
                startActivity(intent)
            }

            R.id.change_lang_menu_option -> {
                val newLang = if (LanguageSelector.getCurrentLanguage() == "en") "es" else "en"
                Log.d("LANGUAGE","NEW LANG: ${newLang}")

                LanguageSelector.saveLanguage(this, newLang)
                Log.d("LANGUAGE","CURRENT LANGUAGE SAVE: ${LanguageSelector.getCurrentLanguage()}")

                LanguageSelector.updateLocale(this, newLang)
                Log.d("LANGUAGE","CURRENT LANGUAGE UPDATE: ${LanguageSelector.getCurrentLanguage()}")

                startActivity(Intent(this, ProfilePageActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }
        return true
    }

    private fun showHighlightsDialog() {
        var selectedItem = 0
        val options = arrayOf(
            getString(R.string.total_routes),
            getString(R.string.riding_time),
            getString(R.string.max_speed),
            getString(R.string.total_distance),
            getString(R.string.longest_route)
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.select_highlights))
            .setPositiveButton("Ok") { _, _ ->
                when (selectedItem) {
                    0 -> getTotalRoutes()
                    1 -> getRidingTime()
                    2 -> getMaxSpeed()
                    3 -> getTotalDistance()
                    4 -> getLongestRoute()
                }
            }
            .setSingleChoiceItems(options, selectedItem) { _, selectedItemIndex ->
                selectedItem = selectedItemIndex
            }
            .setNegativeButton(getString(R.string.cancel_button)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getLongestRoute() {
        binding.selectedHighlightText.text = getString(R.string.longest_route)
    }

    private fun getTotalDistance() {
        binding.selectedHighlightText.text = getString(R.string.total_distance)
    }

    private fun getMaxSpeed() {
        binding.selectedHighlightText.text = getString(R.string.max_speed)
    }

    private fun getRidingTime() {
        binding.selectedHighlightText.text = getString(R.string.riding_time)
    }

    private fun getTotalRoutes() {
        binding.selectedHighlightText.text = getString(R.string.total_routes)
    }


    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_profile
    }


    private fun profileUserMainDataSetup() {
        Log.d("TESTING", "ProfilePageActivity/ binding.userName.text ${binding.userName.text}")

        binding.userName.text =
            UserSessionManager.getCurrentUser()?.nickname ?: "[no_user_selected]"
        setProfileImage(binding.profileImage, UserSessionManager.getCurrentUser()?.profileImage)
        if (UserSessionManager.getCurrentUser()?.profileBanner.isNullOrEmpty()) {
            binding.bannerProfile.setImageResource(R.drawable.ic_profile_banner)
        } else {
            setBannerImage(
                binding.bannerProfile,
                UserSessionManager.getCurrentUser()?.profileBanner
            )
        }
    }

    private fun setProfileImage(imageView: ImageView, imagePath: String?) {
        Log.d("SAVING USER", "imagePath: $imagePath")
        if (imagePath.isNullOrEmpty()) {
            Glide.with(imageView.context)
                .load(R.drawable.user_default_pic)
                .into(imageView)
        } else {
            Glide.with(imageView.context)
                .load(imagePath)
                .into(imageView)
        }
    }

    private fun setBannerImage(imageView: ImageView, imagePath: String?) {
        if (imagePath.isNullOrEmpty()) {
            Glide.with(imageView.context)
                .load(R.drawable.ic_profile_banner)
                .into(imageView)
        } else {
            Glide.with(imageView.context)
                .load(imagePath)
                .into(imageView)
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf(
            getString(R.string.take_picture_dialog),
            getString(R.string.gallery_picture_dialog)
        )
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_image_dialog_title))
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> chooseImageFromGallery()
            }
        }
        builder.setNegativeButton(getString(R.string.cancel_button)) { dialog, _ -> dialog.dismiss() }
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
                        bitmap?.let { bitmap ->
                            currentBannerImagePath?.let { path ->
                                val file = File(path)
                                if (file.exists()) {
                                    file.delete()
                                }
                            }
                            savedPath = ImagePicker.saveImageToInternalStorage(
                                this@ProfilePageActivity, bitmap, "banner-pic-$imageId.jpg"
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
                            bitmap?.let { bitmap ->
                                currentBannerImagePath?.let { path ->
                                    val file = File(path)
                                    if (file.exists()) {
                                        file.delete()
                                    }
                                }
                                savedPath = ImagePicker.saveImageToInternalStorage(
                                    this@ProfilePageActivity, bitmap, "banner-pic-$imageId.jpg"
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
                showSnackbar(getString(R.string.camera_permission_denied_message))
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
            showSnackbar(getString(R.string.no_camera_device_message))
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
                savedPath
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
                savedPath
            )
        }
        UserSessionManager.updateLoggedUser(updatedUser)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.new_user_layout), message, Snackbar.LENGTH_LONG).show()
    }
}