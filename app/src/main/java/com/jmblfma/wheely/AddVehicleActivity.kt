package com.jmblfma.wheely

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.jmblfma.wheely.databinding.NewVehicleLayoutBinding
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.NewVehicleDataViewModel
import java.time.LocalDate

class AddVehicleActivity : AppCompatActivity() {
    private lateinit var binding: NewVehicleLayoutBinding
    private val viewModel: NewVehicleDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewVehicleLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarNewVehicle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbarNewVehicle.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
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
            LocalDate.now().toString()
        )
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