package com.jmblfma.wheely

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.jmblfma.wheely.databinding.TrackViewerBinding
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.utils.MapUtils
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.viewmodels.TrackViewerViewModel

class TrackViewerActivity : NavigationMenuActivity() {
    private lateinit var binding: TrackViewerBinding
    private val viewModel: TrackViewerViewModel by viewModels()
    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_viewer
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TrackViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()
        setupButtonListeners()
        MapUtils.setupMap(binding.mapView, this)
        setupTrackManagement()

        // Forces auto load last
        viewModel.fetchLastTrack()
    }

    // CONTROLS
    private fun setupButtonListeners() {
        binding.buttonLoadPrev.setOnClickListener {
            val success = viewModel.fetchCycle(false)
            success.let { if (!it) { showCycleError() } }
        }
        binding.buttonLoadNext.setOnClickListener {
            val success = viewModel.fetchCycle(true)
            success.let { if (!it) { showCycleError() } }
        }
        binding.buttonDeleteLoadedTrack.setOnClickListener {
            viewModel.deleteLoadedTrack()
        }
        binding.buttonClearUI.setOnClickListener {}
    }

    private fun showCycleError() {
        Snackbar.make(binding.root, getString(R.string.track_cant_cycle_load), Snackbar.LENGTH_SHORT).show()
    }

    // TRACK LOAD MANAGEMENT
    private var numOfTracks = 0
    private fun setupTrackManagement() {
        viewModel.fetchTrackIDs()
        viewModel.fetchTrackCount()
        viewModel.totalNumberOfTracks.observe(this) {
            it?.let {
                numOfTracks = it
                binding.numOfTracks.text = String.format("Tracks: %d", numOfTracks)
            }
        }
        viewModel.trackLoader.observe(this) { track ->
            if (track != null) {
                Log.d("TESTING", "UI/TrackViewer/ TRACK LOADED: $track.trackId")
                MapUtils.loadCompleteRoute(binding.mapView, track.trackData)
                updateTelemetryFromTrack(track)
                // Snackbar.make(binding.root, getString(R.string.track_load_success), Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, getString(R.string.track_load_failure), Snackbar.LENGTH_SHORT).show()
                MapUtils.clearMapAndRefresh(binding.mapView)
                clearTelemetry()
            }
        }
        viewModel.deleteSuccess.observe(this) { success ->
            success?.let {
                if (it) {
                    viewModel.fetchLastTrack()
                    Snackbar.make(binding.root, getString(R.string.track_delete_success), Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, getString(R.string.track_delete_failure), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateTelemetryFromTrack(loadedTrack: Track) {
        binding.trackName.text = loadedTrack.name
        binding.trackDate.text = "<IMPLEMENT>"
        binding.startTime.text = "<startTime>"
        binding.endTime.text = "<endTime>"
        binding.elapsedTime.text = loadedTrack.getFormattedDuration()
        binding.speed.text = loadedTrack.getFormattedAverageSpeedInKmh()
        binding.maxSpeed.text = "<maxSp>"
        binding.distance.text = loadedTrack.getFormattedDistanceInKm()
        binding.trackID.text = String.format("ID_%d", loadedTrack.trackId)
    }
    private fun clearTelemetry() {
        val standByText = getString(R.string.telemetry_standby_placeholder)
        binding.trackName.text = standByText
        binding.trackDate.text = standByText
        binding.startTime.text = standByText
        binding.endTime.text = standByText
        binding.elapsedTime.text = standByText
        binding.speed.text = standByText
        binding.maxSpeed.text = standByText
        binding.distance.text = standByText
        binding.trackID.text = standByText
    }

    // RECOMMENDED FOR OSMDROID IMPLEMENTATION
    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDetach()
    }
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume() // Ensures map tiles and other resources are refreshed
    }
    override fun onPause() {
        super.onPause()
        binding.mapView.onPause() // Ensures any changes or state are paused
    }

}