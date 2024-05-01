package com.jmblfma.wheely

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.android.material.snackbar.Snackbar
import com.jmblfma.wheely.databinding.NewUserLayoutBinding
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.utils.ImagePicker
import com.jmblfma.wheely.utils.ImagePicker.createImageFile
import com.jmblfma.wheely.viewmodels.UserDataViewModel
import java.util.Calendar
import java.util.Locale

class NewUserActivity : AppCompatActivity() {
    private lateinit var binding: NewUserLayoutBinding
    private val calendar = Calendar.getInstance()
    private val viewModel: UserDataViewModel by viewModels()
    private lateinit var profileImageView: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var photoURI: Uri? = null

    companion object {
        private val EMAIL_PATTERN = "^[a-zA-Z0-9_.-]+@[a-zA-Z-]+\\.[a-zA-Z]{2,}$".toRegex()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewUserLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarNewUser)
        setupImagePickerLauncher()
        setupTakePictureLauncher()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbarNewUser.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.userBirthdayEdittext.setOnClickListener {
            showDatePicker()
        }
        binding.addUserButton.setOnClickListener {
            if (!formHasErrors(findViewById(R.id.new_user_layout))) {
                postUser()
                finish()

            }
        }
        binding.addUserImage.setOnClickListener {
            showImageSourceDialog()
        }

        viewModel.userPostStatus.observe(this) { status ->
            status?.let {
                showSnackbar(it)
                binding.userEmailEdittext.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.incorrect_field_data
                    )
                )
                binding.userEmailEdittext.requestFocus()
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
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image")

        builder.setItems(options) { _, which ->
            when (which) {
                0 -> takePicture()
                1 -> chooseImageFromGallery()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()


    }

    private fun setupImagePickerLauncher() {
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    profileImageView.setImageURI(it)
                    val bitmap = ImagePicker.getBitmapFromUri(this, it)
                    bitmap?.let {
                        val savedPath = ImagePicker.saveImageToInternalStorage(this, it, "profile_image.png")
                    }

                    //saveImageToInternalStorage(it)
                }
            }
    }

    private fun setupTakePictureLauncher() {
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    photoURI?.let {
                        profileImageView.setImageURI(it)
                        //saveImageToInternalStorage(it)
                    }
                }
            }
    }
    private fun takePicture() {
        // Ensure the device has a camera
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            // Create a file URI to save the image
            photoURI = createImageFile(this)  // This would use your FileUtils class to create a file
            takePictureLauncher.launch(photoURI)  // Launch the camera activity
        } else {
            Toast.makeText(this, "This device does not have a camera", Toast.LENGTH_SHORT).show()
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
        val newUser = User(
            0,
            binding.userNicknameEdittext.text.toString(),
            binding.userFirstnameEdittext.text.toString(),
            binding.userLastnameEdittext.text.toString(),
            binding.userEmailEdittext.text.toString(),
            binding.userBirthdayEdittext.text.toString()
        )
        viewModel.addUser(newUser)
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this, { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
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
        Snackbar.make(findViewById(R.id.new_user_layout), message, Snackbar.LENGTH_LONG)
            .show()
    }

}