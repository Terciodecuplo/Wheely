package com.jmblfma.wheely

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jmblfma.wheely.adapter.ProfileViewPagerAdapter
import com.jmblfma.wheely.databinding.UserProfileMainBinding
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.utils.ImagePicker
import com.jmblfma.wheely.utils.ImageWorkerUtil
import com.jmblfma.wheely.utils.LanguageSelector
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.utils.PermissionsManager
import com.jmblfma.wheely.utils.TrackAnalysis
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.UserDataViewModel

class ProfilePageActivity : NavigationMenuActivity() {
    private lateinit var binding: UserProfileMainBinding
    private lateinit var updateReceiver: BroadcastReceiver
    private var trackHistoryList: List<Track> = emptyList()
    private var userVehicleList: List<Vehicle> = emptyList()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private val viewModel: UserDataViewModel by viewModels()
    private var photoURI: Uri? = null
    private var imActive: Boolean = false

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
    }

    @SuppressLint("UseCompatLoadingForDrawables", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserProfileMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        LanguageSelector.updateLocale(this, LanguageSelector.loadLanguage(this))
        setupObservers()
        setupFragments()
        setupBottomNavigation()
        setupToolbar()
        setupImagePickerLauncher()
        setupTakePictureLauncher()
    }

    private fun setupUserFields() {
        binding.totalTracksValue.text = trackHistoryList.size.toString()
        binding.totalRidingTime.text = TrackAnalysis.getTracksTotalDuration(trackHistoryList)
        binding.totalDistanceValue.text = TrackAnalysis.getTracksTotalDistanceInKm(trackHistoryList)
        if (trackHistoryList.isNotEmpty()) {
            when (UserSessionManager.getPreferredHighlight()){
                1->{
                    getMaxSpeed()
                }
                2->{
                    getLongestRoute()
                }
                3->{
                    getNumberOfVehicles()
                }
                4->{
                    getMaxAltitude()
                }
                else->{
                    binding.selectedHighlightText.text = getString(R.string.total_routes)
                    binding.selectedHighlightValue.text = "0"
                }
            }

        }
    }

    private fun setupObservers() {
        viewModel.userTrackList.observe(this) {
            if (it != null) {
                trackHistoryList = it
                // Log.d("TESTING", "TrackHistoyList State = $trackHistoryList ----- and size ${trackHistoryList.size}")
                setupUserFields()
            }
        }
        viewModel.vehicleList.observe(this) {
            if (it != null) userVehicleList = it
        }
    }

    private fun setupFragments() {
        val viewPager: ViewPager2 = binding.viewPager
        val tabLayout: TabLayout = binding.tabLayout
        val profileViewPagerAdapter = ProfileViewPagerAdapter(this)

        binding.viewPager.adapter = profileViewPagerAdapter

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
        imActive = true
        setupUpdateReceiver()
        profileUserMainDataSetup()
        val userId = UserSessionManager.getCurrentUser()!!.userId
        viewModel.fetchTrackListByUser(userId)
        viewModel.fetchUserVehicles(userId)
    }

    override fun onPause() {
        super.onPause()
        imActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        imActive = false
        unregisterReceiver(updateReceiver)
    }

    private fun setupUpdateReceiver() {
        updateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "com.jmblfma.wheely.UPDATE_USER_INFO") {
                    // Log.d("SaveImageWorker", "Broadcast received")
                    if (imActive) restoreUI()
                }
            }
        }

        val intentFilter = IntentFilter("com.jmblfma.wheely.UPDATE_USER_INFO")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // use the new method with RECEIVER_NOT_EXPORTED
            registerReceiver(updateReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            // use the legacy method
            registerReceiver(updateReceiver, intentFilter)
        }
    }

    private fun restoreUI() {

        if(UserSessionManager.getCurrentUser()?.profileBanner != null){
            binding.bannerProfile.setImageBitmap(BitmapFactory.decodeFile(UserSessionManager.getCurrentUser()?.profileBanner))
        } else {
            binding.bannerProfile.setImageResource(R.drawable.ic_profile_banner)
        }
        if(UserSessionManager.getCurrentUser()?.profileImage != null){
            binding.profileImage.setImageBitmap(BitmapFactory.decodeFile(UserSessionManager.getCurrentUser()?.profileImage))
        } else {
            binding.profileImage.setImageResource(R.drawable.user_default_pic)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Check if HomeActivity is in the back stack
        val startMain = Intent(this, HomePageActivity::class.java)
        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(startMain)
        finish()
    }

    private fun setupToolbar() {
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
            R.id.user_stats_menu_option -> {
                val intent = Intent(this, UserStatsActivity::class.java)
                startActivity(intent)
            }

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
                // Log.d("LANGUAGE", "NEW LANG: ${newLang}")

                LanguageSelector.saveLanguage(this, newLang)
                // Log.d("LANGUAGE", "CURRENT LANGUAGE SAVE: ${LanguageSelector.getCurrentLanguage()}")

                LanguageSelector.updateLocale(this, newLang)
                // Log.d("LANGUAGE", "CURRENT LANGUAGE UPDATE: ${LanguageSelector.getCurrentLanguage()}")

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
            getString(R.string.max_speed),
            getString(R.string.longest_route),
            getString(R.string.number_of_vehicles),
            getString(R.string.max_altitude)
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.select_highlights))
            .setPositiveButton("Ok") { _, _ ->
                when (selectedItem) {
                    0 -> getMaxSpeed()
                    1 -> getLongestRoute()
                    2 -> getNumberOfVehicles()
                    3 -> getMaxAltitude()
                }
            }
            .setSingleChoiceItems(options, selectedItem) { _, selectedItemIndex ->
                selectedItem = selectedItemIndex
            }
            .setNegativeButton(getString(R.string.cancel_button)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getLongestRoute() {
        UserSessionManager.highlightPreference(2)
        binding.selectedHighlightText.text = getString(R.string.longest_route)
        binding.selectedHighlightValue.text = TrackAnalysis.getLongestTrackInKm(trackHistoryList)

    }

    private fun getNumberOfVehicles() {
        UserSessionManager.highlightPreference(3)

        binding.selectedHighlightText.text = getString(R.string.number_of_vehicles)
        binding.selectedHighlightValue.text = userVehicleList.size.toString()
    }

    private fun getMaxAltitude() {
        UserSessionManager.highlightPreference(4)

        binding.selectedHighlightText.text = getString(R.string.max_altitude)
        binding.selectedHighlightValue.text = TrackAnalysis.getTracksMaxAltitude(trackHistoryList)
    }


    private fun getMaxSpeed() {
        UserSessionManager.highlightPreference(1)
        binding.selectedHighlightText.text = getString(R.string.max_speed)
        binding.selectedHighlightValue.text = TrackAnalysis.getTracksMaxSpeed(trackHistoryList)
    }


    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_profile
    }


    private fun profileUserMainDataSetup() {
        // Log.d("TESTING", "TRACKHISTORY IS NOT EMPTY ${trackHistoryList.size}")
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
        // Log.d("SaveImageWorker", "imagePath: $imagePath")
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
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let { receivedUri ->
                    Glide.with(this@ProfilePageActivity).load(receivedUri)
                        .into(binding.bannerProfile)
                    processImageAndSave(receivedUri, "user", "banner", "user-banner")
                }
            }
    }

    private fun setupTakePictureLauncher() {
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    photoURI?.let { receivedUri ->
                        binding.bannerProfile.setImageURI(receivedUri)
                        processImageAndSave(receivedUri, "user", "banner", "user-banner")
                    }
                }
            }
    }

    private fun processImageAndSave(
        uri: Uri,
        entityType: String,
        imageType: String,
        prefix: String
    ) {
        val entityId = UserSessionManager.getCurrentUser()?.userId
        ImageWorkerUtil.enqueueImageSave(
            this,
            uri,
            entityId,
            entityType,
            imageType,
            prefix
        )
        // Log.d("SaveImageWorker", "Enqueuing image save: uri=$uri, entityId=$entityId, entityType=$entityType, imageType=$imageType, fileName=$prefix")

    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            takePicture()
        } else {
            PermissionsManager.getCameraPermission(this, CAMERA_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                takePicture()
            } else {
                // Permission is denied
                Toast.makeText(this, getString(R.string.camera_permission_denied_message), Toast.LENGTH_SHORT).show()
                Toast.makeText(this, getString(R.string.permissions_denied_extended), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun takePicture() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            photoURI = ImagePicker.createImageFile(this)
            takePictureLauncher.launch(photoURI)
        } else {
            showSnackbar(getString(R.string.no_camera_device_message))
        }
    }

    private fun chooseImageFromGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun updateBannerUI() {
        // Log.d("SaveImageWorker", "Banner RELOADED => ${UserSessionManager.getCurrentUser()?.profileBanner}")
        Glide.with(this)
            .load(UserSessionManager.getCurrentUser()?.profileBanner)
            .into(binding.bannerProfile)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.new_user_layout), message, Snackbar.LENGTH_LONG).show()
    }
}