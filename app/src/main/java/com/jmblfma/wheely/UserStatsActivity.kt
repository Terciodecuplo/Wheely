package com.jmblfma.wheely

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.jmblfma.wheely.databinding.UserStatsLayoutBinding
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.utils.ImagePicker
import com.jmblfma.wheely.utils.PermissionsManager
import com.jmblfma.wheely.utils.TrackAnalysis
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.UserDataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class UserStatsActivity : AppCompatActivity() {
    private lateinit var binding: UserStatsLayoutBinding
    private val calendar = Calendar.getInstance()
    private val viewModel: UserDataViewModel by viewModels()
    private var trackList: List<Track> = emptyList()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var photoURI: Uri? = null
    private var savedPath: String? = null

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserStatsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupImagePickerLauncher()
        setupTakePictureLauncher()
        setSupportActionBar(binding.toolbarUserStats)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTitle.text = getString(R.string.stats_layout_title)
        binding.toolbarUserStats.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        setupObservers()
        getUserData()
        binding.submitChangesButton.setOnClickListener {
            submitChangesDialog()
        }
        binding.userBirthdayEdittext.setOnClickListener {
            showDatePicker()
        }
        binding.editUserImage.setOnClickListener {
            showImageSourceDialog()
        }

    }

    private fun setupObservers() {
        viewModel.userTrackList.observe(this) {
            trackList = it
            setupUserStats()
        }
    }

    private fun setupUserStats() {
        if(trackList.isNotEmpty()) {
            binding.totalTimeValue.text = TrackAnalysis.getTracksTotalDuration(trackList)
            binding.maxSpeedValue.text = TrackAnalysis.getTracksMaxSpeed(trackList)
            binding.totalDistanceValue.text = TrackAnalysis.getTracksTotalDistanceInKm(trackList)
            binding.avgSpeedValue.text = TrackAnalysis.getTracksAverageSpeedInKmh(trackList)
            binding.longestRouteValue.text = TrackAnalysis.getLongestTrackInKm(trackList)
            binding.maxDurationValue.text = TrackAnalysis.getTracksMaxDuration(trackList)
            binding.maxAltitudeValue.text = TrackAnalysis.getTracksMaxAltitude(trackList)
        }
        binding.totalRoutesValue.text = trackList.size.toString()
    }

    override fun onResume() {
        super.onResume()
        val userId = UserSessionManager.getCurrentUser()!!.userId
        viewModel.fetchTrackListByUser(userId)
    }

    private fun submitChanges() {
        if (!formHasErrors(findViewById(R.id.statsLayout))) {
            updateUser()
        }
        blockDataEdition()
    }

    private fun updateUser() {
        UserSessionManager.getCurrentUser()?.let {
            viewModel.updateUserPersonalInfo(
                it.userId,
                binding.userNicknameEdittext.text.toString(),
                binding.userFirstnameEdittext.text.toString(),
                binding.userLastnameEdittext.text.toString(),
                binding.userBirthdayEdittext.text.toString(),
                savedPath
            )
        }
        viewModel.fetchUser(binding.userEmailEdittext.text.toString())
        viewModel.fetchedUser.observe(this) {
            UserSessionManager.updateLoggedUser(it)
        }
    }

    private fun submitChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Changes")
            .setMessage("Are you sure you want to submit these changes?")
            .setPositiveButton("Confirm") { _, _ ->
                Toast.makeText(this, "User data submitted!", Toast.LENGTH_SHORT).show()
                submitChanges()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                blockDataEdition()
                Toast.makeText(this, "Edition cancelled", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }

    private fun removeUserDialog() {
        AlertDialog.Builder(this)
            .setTitle("Remove User")
            .setMessage("This action cannot be undone. Are you sure you want to remove the current user?")
            .setPositiveButton("Confirm") { _, _ ->
                Toast.makeText(this, "Current user deleted!", Toast.LENGTH_SHORT).show()
                removeUserFromDataBase()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun removeUserFromDataBase() {
        viewModel.deleteUser(UserSessionManager.getCurrentUser()!!.email)
        UserSessionManager.logoutUser()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun manageFields(editText: EditText, isEditable: Boolean) {
        if (isEditable) {
            editText.isClickable = true
            editText.isFocusable = true
            editText.isFocusableInTouchMode = true
            editText.isCursorVisible = true
        } else {
            editText.isClickable = false
            editText.isFocusable = false
            editText.isFocusableInTouchMode = false
            editText.isCursorVisible = false
        }
    }

    private fun setProfileImage(imageView: ImageView, imagePath: String?) {
        if (imagePath.isNullOrEmpty()) {
            Glide.with(imageView.context)
                .load(R.drawable.user_default_pic) // Your placeholder drawable
                .into(imageView)
        } else {
            Glide.with(imageView.context)
                .load(imagePath)
                .into(imageView)
        }
    }

    private fun getUserData() {
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
                removeUserDialog()
            }
        }
        return true
    }

    private fun editUserData() {
        val isEditable = true
        manageFields(binding.userNicknameEdittext, isEditable)
        manageFields(binding.userFirstnameEdittext, isEditable)
        manageFields(binding.userLastnameEdittext, isEditable)
        binding.userEmailEdittext.setTextColor(getColor(R.color.subtext_grey))
        binding.userBirthdayEdittext.isClickable = true
        binding.editUserImage.visibility = View.VISIBLE
        binding.submitChangesContainer.visibility = View.VISIBLE
        val layoutParams = binding.userStatsContainer.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = (15 * applicationContext.resources.displayMetrics.density).toInt()
        binding.userStatsContainer.layoutParams = layoutParams
        if(binding.scrollView.scrollY == (binding.scrollView.getChildAt(0).measuredHeight - binding.scrollView.measuredHeight)){
            binding.scrollView.post {
                binding.scrollView.smoothScrollTo(0, binding.scrollView.getChildAt(0).height)
            }
        }
    }

    private fun blockDataEdition() {
        startActivity(Intent(this, UserStatsActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                binding.userBirthdayEdittext.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
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
        val currentProfileImagePath = UserSessionManager.getCurrentUser()?.profileImage
        Log.d("UPDATE PIC", "Current: $currentProfileImagePath")
        val imageId = UUID.randomUUID().toString()
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let { receivedUri ->
                    // Use Glide to load and display the image without delays
                    Glide
                        .with(this@UserStatsActivity)
                        .load(receivedUri)
                        .into(binding.userImage)
                    val bitmap = ImagePicker.fixImageOrientation(this, uri)
                    // Save the image asynchronously
                    lifecycleScope.launch(Dispatchers.IO) {
                        bitmap?.let { bitmap ->
                            currentProfileImagePath?.let { path ->
                                val file = File(path)
                                if (file.exists()) {
                                    file.delete()
                                }
                            }
                            savedPath = ImagePicker.saveImageToInternalStorage(
                                this@UserStatsActivity, bitmap, "profile-pic-$imageId.jpg"
                            )
                        }
                        Log.d("UPDATE PIC", "Saved path from storage: $savedPath")
                    }
                }
            }
    }

    private fun setupTakePictureLauncher() {
        val currentProfileImagePath = UserSessionManager.getCurrentUser()?.profileImage
        val imageId = UUID.randomUUID().toString()
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    photoURI?.let { receivedUri ->
                        binding.userImage.setImageURI(receivedUri)
                        val bitmap = ImagePicker.fixImageOrientation(this, receivedUri)
                        lifecycleScope.launch(Dispatchers.IO) {
                            bitmap?.let { bitmap ->
                                currentProfileImagePath?.let { path ->
                                    val file = File(path)
                                    if (file.exists()) {
                                        file.delete()
                                    }
                                }
                                savedPath = ImagePicker.saveImageToInternalStorage(
                                    this@UserStatsActivity, bitmap, "profile-pic-$imageId.jpg"
                                )
                            }
                            Log.d("UPDATE PIC", "Saved path from camera: $savedPath")
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

    private fun formHasErrors(view: View): Boolean {
        var hasError = false

        if (view is EditText) {

            if (view.text.toString().trim().isEmpty()) {
                view.error = getString(R.string.form_error_empty_field)
                hasError = true
            }

            if (!hasError) {
                view.error = null
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                if (formHasErrors(view.getChildAt(i))) {
                    hasError = true
                }
            }
        }

        return hasError
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.new_user_layout), message, Snackbar.LENGTH_LONG).show()
    }

}

