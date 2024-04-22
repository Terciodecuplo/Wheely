package com.jmblfma.wheely

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.jmblfma.wheely.databinding.NewVehicleLayoutBinding
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.viewmodels.NewVehicleDataViewModel
import java.time.LocalDate

class AddVehicleActivity : AppCompatActivity() {
    private lateinit var binding: NewVehicleLayoutBinding
    private val viewModel: NewVehicleDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewVehicleLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.addVehicleButton.setOnClickListener{
            viewModel.addVehicle(getNewVehicleData())
        }
    }

    private fun getNewVehicleData(): Vehicle {
        val horsepower: Int = try {
            Integer.parseInt(binding.newVehicleHorsepowerEdittext.text.toString())
        } catch (e: NumberFormatException) {
            0
        }
        return Vehicle( 0, 13,
            binding.newVehicleNameEdittext.text.toString(),
            binding.newVehicleBrandEdittext.text.toString(),
            binding.newVehicleModelEdittext.text.toString(),
            binding.newVehicleYearEdittext.text.toString(),
            horsepower,
            LocalDate.now().toString()
        )
    }
}