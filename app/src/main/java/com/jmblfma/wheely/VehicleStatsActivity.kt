package com.jmblfma.wheely

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.jmblfma.wheely.databinding.VehicleStatsLayoutBinding
import com.jmblfma.wheely.viewmodels.NewVehicleDataViewModel

class VehicleStatsActivity: AppCompatActivity() {
    private lateinit var binding : VehicleStatsLayoutBinding
    private val viewModel : NewVehicleDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = VehicleStatsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getVehicleData()
    }

    private fun getVehicleData() {

    }
}