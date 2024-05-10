package com.jmblfma.wheely

import android.Manifest
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
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
import com.jmblfma.wheely.databinding.NewVehicleLayoutBinding
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.utils.ImagePicker
import com.jmblfma.wheely.utils.PermissionsManager
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.NewVehicleDataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID


class AddVehicleActivity : AppCompatActivity() {
    private lateinit var binding: NewVehicleLayoutBinding
    private val viewModel: NewVehicleDataViewModel by viewModels()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var photoURI: Uri? = null
    private var savedPath: String? = null
    private lateinit var userCandidate: User

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewVehicleLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setupImagePickerLauncher()
        setupTakePictureLauncher()
        setSupportActionBar(binding.toolbarNewVehicle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTitle.text = getString(R.string.add_vehicle_layout_title)
        binding.toolbarNewVehicle.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        val signUpState = intent.extras?.getBoolean("signUpState") ?: false
        if (!signUpState) {
            binding.progressBar.visibility = View.INVISIBLE
            binding.stepTextview.visibility = View.INVISIBLE
            binding.addVehicleButton.text = getString(R.string.add_vehicle_button)
        } else {
            binding.addVehicleButton.text = getString(R.string.signup_button)
            viewModel.getUserCandidate()
            viewModel.userCandidateData.observe(this) {
                if (it != null) {
                    Log.d("USERDATA", "user posted = $it")
                    userCandidate = it
                }

            }
        }
        binding.addVehiclePreviewImage.setOnClickListener {
            showImageSourceDialog()
        }
        binding.addVehicleButton.setOnClickListener {
            if (!formHasErrors(findViewById(R.id.new_vehicle_layout))) {
                val newVehicle = setNewVehicleData(signUpState)
                if (signUpState) {
                    viewModel.insertVehicleWithNewUser(userCandidate, newVehicle)
                    viewModel.userAdditionStatus.observe(this) {
                        showSnackbar(it)
                    }
                    setResult(RESULT_OK) // Calls back the result to ParentActivity so it can finish
                } else{
                    viewModel.addVehicle(newVehicle)
                    viewModel.vehiclePostStatus.observe(this){
                        if (it == true){
                            Toast.makeText(this, getString(R.string.new_vehicle_notification), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, getString(R.string.add_vehicle_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                finish()
            }
        }
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val earliestYear = 1885 //First motorcycle invention


        binding.newVehicleYearEdittext.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val enteredText = v.text.toString()
                if (enteredText.isNotEmpty()) {
                    try {
                        val year = enteredText.toInt()
                        if (year < earliestYear || year > currentYear) {
                            binding.newVehicleYearEdittext.error =
                                getString(R.string.year_range_error, earliestYear, currentYear)
                            if (year < earliestYear) {
                                showSnackbar(getString(R.string.funny_info))
                            }
                            true
                        } else {
                            false
                        }
                    } catch (e: NumberFormatException) {
                        binding.newVehicleYearTextview.error = "Invalid input"
                        true
                    }
                } else {
                    false
                }
            } else {
                false
            }
        }

    }

    private fun setNewVehicleData(signUpState: Boolean): Vehicle {
        var userId = 0
        if(!signUpState) {
            userId = UserSessionManager.getCurrentUser()?.userId!!
        }


        val horsepower: Int = try {
            Integer.parseInt(binding.newVehicleHorsepowerEdittext.text.toString())
        } catch (e: NumberFormatException) {
            0
        }
        return Vehicle(
            0,
            userId,
            binding.newVehicleNameEdittext.text.toString(),
            binding.newVehicleBrandEdittext.text.toString(),
            binding.newVehicleModelEdittext.text.toString(),
            binding.newVehicleYearEdittext.text.toString(),
            horsepower,
            LocalDate.now().toString(),
            savedPath
        )
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
        val imageId = UUID.randomUUID().toString()
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let { receivedUri ->
                    // Use Glide to load and display the image without delays
                    Glide
                        .with(this@AddVehicleActivity)
                        .load(receivedUri)
                        .into(binding.vehiclePreviewImage)
                    val bitmap = ImagePicker.fixImageOrientation(this, uri)
                    // Save the image asynchronously
                    lifecycleScope.launch(Dispatchers.IO) {
                        bitmap?.let {
                            savedPath = ImagePicker.saveImageToInternalStorage(
                                this@AddVehicleActivity, it, "vehicle-pic-$imageId.jpg"
                            )
                        }
                    }
                }
            }
    }

    private fun setupTakePictureLauncher() {
        val imageId = UUID.randomUUID().toString()
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    photoURI?.let { receivedUri ->
                        binding.vehiclePreviewImage.setImageURI(receivedUri)
                        val bitmap = ImagePicker.fixImageOrientation(this, receivedUri)
                        lifecycleScope.launch(Dispatchers.IO) {
                            bitmap?.let {
                                savedPath = ImagePicker.saveImageToInternalStorage(
                                    this@AddVehicleActivity, it, "vehicle-pic-$imageId.jpg"
                                )
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
                // Permission was granted
                takePicture()
            } else {
                // Permission was denied
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
        Snackbar.make(findViewById(R.id.new_vehicle_layout), message, Snackbar.LENGTH_LONG).show()
    }
}