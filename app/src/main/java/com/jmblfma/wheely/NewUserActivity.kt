package com.jmblfma.wheely

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.jmblfma.wheely.databinding.NewUserLayoutBinding
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.utils.ImagePicker
import com.jmblfma.wheely.utils.ImagePicker.createImageFile
import com.jmblfma.wheely.utils.ImagePicker.fixImageOrientation
import com.jmblfma.wheely.utils.ImageWorkerUtil
import com.jmblfma.wheely.utils.PermissionsManager
import com.jmblfma.wheely.utils.SignUpManager
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.UserDataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class NewUserActivity : AppCompatActivity() {
    private lateinit var binding: NewUserLayoutBinding
    private val calendar = Calendar.getInstance()
    private val viewModel: UserDataViewModel by viewModels()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var photoURI: Uri? = null

    companion object {
        private val EMAIL_PATTERN = "^[a-zA-Z0-9_.-]+@[a-zA-Z-]+\\.[a-zA-Z]{2,}$".toRegex()
        private const val CAMERA_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewUserLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarNewUser)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setupImagePickerLauncher()
        setupTakePictureLauncher()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTitle.text = getString(R.string.signup_layout_title)
        binding.toolbarNewUser.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            SignUpManager.restoreState()
        }
        binding.userBirthdayEdittext.setOnClickListener {
            showDatePicker()
        }
        resultLauncher =
                // Finish ParentActivity based on result from ChildActivity
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    finish()
                }
            }
        binding.addUserButton.setOnClickListener {
            if (!formHasErrors(findViewById(R.id.new_user_layout))) {
                viewModel.fetchUser(binding.userEmailEdittext.text.toString())

            }
        }
        binding.addUserImage.setOnClickListener {
            showImageSourceDialog()
        }

        viewModel.fetchedUser.observe(this) {
            if (it != null) {
                showSnackbar(getString(R.string.email_in_use_error))
                binding.userEmailEdittext.setTextColor(
                    ContextCompat.getColor(
                        this, R.color.incorrect_field_data
                    )
                )
                binding.userEmailEdittext.requestFocus()
            } else {
                postUser()
                val intent = Intent(this, AddVehicleActivity::class.java)
                intent.putExtra("signUpState", true)
                resultLauncher.launch(intent)
            }
        }
        viewModel.userUpdateStatus.observe(this) { status ->
            status?.let {
                showSnackbar(it)
            }

        }
        binding.userEmailEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            @SuppressLint("PrivateResource")
            override fun afterTextChanged(s: Editable?) {
                binding.userEmailEdittext.setTextColor(
                    ContextCompat.getColor(
                        binding.userEmailEdittext.context,
                        com.google.android.material.R.color.m3_default_color_primary_text
                    )
                )
            }
        })
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
                    Glide.with(this@NewUserActivity)
                        .load(receivedUri)
                        .into(binding.userImage)
                    processImageAndSave(receivedUri, "user", "profile", "profile-pic")
                }
            }
    }

    private fun setupTakePictureLauncher() {
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    photoURI?.let { receivedUri ->
                        binding.userImage.setImageURI(receivedUri)
                        Log.d("SaveImageWorker", "URI candidate = $receivedUri")
                        processImageAndSave(receivedUri, "user", "profile", "profile-pic")
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
        val entityId = -1
        ImageWorkerUtil.enqueueImageSave(
            this,
            uri,
            entityId,
            entityType,
            imageType,
            prefix
        )
        Log.d(
            "SaveImageWorker",
            "Enqueuing image save: uri=$uri, entityId=$entityId, entityType=$entityType, imageType=$imageType, fileName=$prefix"
        )

    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            takePicture()
        } else {
            PermissionsManager.getCameraPermission(this, NewUserActivity.CAMERA_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NewUserActivity.CAMERA_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePicture()
        } else {
            showSnackbar(getString(R.string.camera_permission_denied_message))
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


    private fun formHasErrors(view: View): Boolean {
        var hasError = false

        if (view is EditText) {

            if (view.text.toString().trim().isEmpty()) {
                view.error = getString(R.string.form_error_empty_field)
                hasError = true
            }

            if (view.id == R.id.user_email_edittext && !EMAIL_PATTERN.matches(view.text.toString())) {
                if (view.text.toString().isNotEmpty()) {
                    view.error = getString(R.string.form_error_invalid_email_format)
                    hasError = true
                }
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

    private fun postUser() {
        Log.d("SaveImageWorker", "Candidate profile image path = ${SignUpManager.userProfilePictureCandidate}")
        viewModel.setUserCandidate(
            User(
                0,
                binding.userNicknameEdittext.text.toString(),
                binding.userFirstnameEdittext.text.toString(),
                binding.userLastnameEdittext.text.toString(),
                binding.userEmailEdittext.text.toString(),
                binding.userBirthdayEdittext.text.toString(),
                SignUpManager.userProfilePictureCandidate,
                null
            )
        )
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

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.new_user_layout), message, Snackbar.LENGTH_LONG).show()
    }

}