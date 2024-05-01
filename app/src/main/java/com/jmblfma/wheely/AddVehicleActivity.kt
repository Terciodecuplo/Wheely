package com.jmblfma.wheely

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.jmblfma.wheely.databinding.NewVehicleLayoutBinding
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.utils.ImagePicker
import com.jmblfma.wheely.utils.PermissionsManager
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.NewVehicleDataViewModel
import kotlinx.coroutines.CoroutineScope
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

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewVehicleLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setupImagePickerLauncher()
        setupTakePictureLauncher()
        setSupportActionBar(binding.toolbarNewVehicle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbarNewVehicle.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.addVehiclePreviewImage.setOnClickListener {
            showImageSourceDialog()
        }
        binding.addVehicleButton.setOnClickListener {
            if (!formHasErrors(findViewById(R.id.new_vehicle_layout))) {
                val newVehicle = setNewVehicleData()
                viewModel.addVehicle(newVehicle)
                finish()
            }
        }
        viewModel.vehiclePostStatus.observe(this) { status ->
            status?.let {
                showSnackbar(it)
            }
        }

    }


    private fun setNewVehicleData(): Vehicle {
        val horsepower: Int = try {
            Integer.parseInt(binding.newVehicleHorsepowerEdittext.text.toString())
        } catch (e: NumberFormatException) {
            0
        }
        return Vehicle(
            0,
            UserSessionManager.getCurrentUser()?.userId ?: -1,
            binding.newVehicleNameEdittext.text.toString(),
            binding.newVehicleBrandEdittext.text.toString(),
            binding.newVehicleModelEdittext.text.toString(),
            binding.newVehicleYearEdittext.text.toString(),
            horsepower,
            LocalDate.now().toString(),
            savedPath.toString()
        )
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
                    CoroutineScope(Dispatchers.IO).launch {
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
                        CoroutineScope(Dispatchers.IO).launch {
                            val bitmap =
                                ImagePicker.getBitmapFromUri(this@AddVehicleActivity, receivedUri)
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