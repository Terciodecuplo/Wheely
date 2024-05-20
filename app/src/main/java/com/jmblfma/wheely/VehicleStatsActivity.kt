package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jmblfma.wheely.databinding.VehicleStatsLayoutBinding
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.utils.TrackAnalysis
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.NewVehicleDataViewModel

class VehicleStatsActivity : AppCompatActivity() {
    private lateinit var binding: VehicleStatsLayoutBinding
    private val viewModel: NewVehicleDataViewModel by viewModels()
    private var vehicleTrackList: List<Track> = emptyList()
    private lateinit var vehicle: Vehicle
    private var vehicleId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = VehicleStatsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupObservers()
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
        // Log.d("VEHICLE", "vehicleID = $vehicleId")
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchSingleVehicle(vehicleId)
        viewModel.getVehicleTrackList(vehicleId)
    }

    private fun setupObservers() {
        viewModel.vehicleData.observe(this) {
            vehicle = it
            viewModel.vehicleTrackList.observe(this) { list ->
                vehicleTrackList = list
                getVehicleData()
            }
        }

    }

    private fun getVehicleData() {
        // Log.d("TESTING", "Observer says = ${vehicle.name} size of the list = ${vehicleTrackList.size}")

        setVehicleImage(binding.vehicleImage, vehicle.image)
        binding.vehicleNameEdittext.setText(vehicle.name)
        binding.vehicleBrandEdittext.setText(vehicle.brand)
        binding.vehicleModelEdittext.setText(vehicle.model)
        binding.hpEdittext.setText(vehicle.horsepower.toString())
        binding.yearEdittext.setText(vehicle.year)
        binding.totalRidingTime.text = TrackAnalysis.getTracksTotalDuration(vehicleTrackList, true)
        binding.totalDistanceValue.text = TrackAnalysis.getTracksTotalDistanceInKm(vehicleTrackList)
        binding.maxSpeedValue.text = TrackAnalysis.getTracksMaxSpeed(vehicleTrackList)
        binding.totalTracksValue.text = vehicleTrackList.size.toString()

    }

    private fun setVehicleImage(imageView: ImageView, imagePath: String?) {
        if (imagePath.isNullOrEmpty()) {
            Glide.with(imageView.context)
                .load(R.drawable.vehicle_placeholder)
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
                // Log.d("VEHICLE", "vehicle removed -> Show dialog")
                removeVehicleDialog()
            }
        }
        return true
    }

    private fun removeVehicleDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.remove_vehicle_dialog_title))
            .setMessage(getString(R.string.remove_vehicle_dialog_message))
            .setPositiveButton(getString(R.string.confirm_button)) { _, _ ->
                removeVehicleFromDataBase()
            }
            .setNegativeButton(getString(R.string.cancel_button)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun removeVehicleFromDataBase() {
        UserSessionManager.getCurrentUser()?.userId?.let { viewModel.deleteVehicle(vehicleId, it) }
        viewModel.removeVehicleStatus.observe(this) {
            if (it != 0) {
                Toast.makeText(
                    this,
                    getString(R.string.remove_vehicle_notification),
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, ProfilePageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.remove_vehicle_error), Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }
}