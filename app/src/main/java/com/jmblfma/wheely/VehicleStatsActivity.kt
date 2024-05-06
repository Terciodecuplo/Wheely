package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.jmblfma.wheely.databinding.VehicleStatsLayoutBinding
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.NewVehicleDataViewModel

class VehicleStatsActivity : AppCompatActivity() {
    private lateinit var binding: VehicleStatsLayoutBinding
    private val viewModel: NewVehicleDataViewModel by viewModels()
    private var vehicleId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = VehicleStatsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarVehicleStats)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTitle.text = getString(R.string.stats_layout_title)
        binding.toolbarVehicleStats.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        vehicleId = intent.extras?.getInt("vehicleId")
            ?: throw IllegalArgumentException("Vehicle ID not found")
        Log.d("VEHICLE", "vehicleID = $vehicleId")
        getVehicleData(vehicleId)
    }

    private fun getVehicleData(vehicleId: Int) {
        viewModel.fetchSingleVehicle(vehicleId)
        viewModel.vehicleData.observe(this){
            Log.d("VEHICLE", "Observer says = ${it.name}")
            setVehicleImage(binding.vehicleImage, it.image)
            binding.vehicleNameEdittext.setText(it.name)
            binding.vehicleBrandEdittext.setText(it.brand)
            binding.vehicleModelEdittext.setText(it.model)
            binding.hpEdittext.setText(it.horsepower.toString())
            binding.yearEdittext.setText(it.year)
        }
    }

    private fun setVehicleImage(imageView: ImageView, imagePath: String?) {
        if (imagePath.isNullOrEmpty()) {
            Glide.with(imageView.context)
                .load(R.drawable.pic_vehicle_template) // Your placeholder drawable
                .into(imageView)
        } else {
            Glide.with(imageView.context)
                .load(imagePath)
                .into(imageView)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.vehicle_stats_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.remove_vehicle_menu_option -> {
                Log.d("VEHICLE","vehicle removed -> Show dialog")
                removeVehicleDialog()
            }
        }
        return true
    }

    private fun removeVehicleDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.remove_vehicle_dialog_title))
            .setMessage(getString(R.string.remove_vehicle_dialog_message))
            .setPositiveButton(getString(R.string.confirm_button)) { dialog, which ->
                Toast.makeText(this, getString(R.string.remove_vehicle_notification), Toast.LENGTH_SHORT).show()
                removeVehicleFromDataBase()
            }
            .setNegativeButton(getString(R.string.cancel_button)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun removeVehicleFromDataBase(){
        viewModel.deleteVehicle(vehicleId)
        val intent = Intent(applicationContext, ProfilePageActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}