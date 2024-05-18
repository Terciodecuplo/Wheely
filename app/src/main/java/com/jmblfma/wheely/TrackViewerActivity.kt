package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.jmblfma.wheely.databinding.TrackViewerBinding
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.utils.DialogUtils
import com.jmblfma.wheely.utils.LanguageSelector
import com.jmblfma.wheely.utils.MapUtils
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.utils.StyleUtils
import com.jmblfma.wheely.utils.TrackAnalysis
import com.jmblfma.wheely.utils.TrackAnalysis.calculateElevationInMeters
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

        LanguageSelector.updateLocale(this, LanguageSelector.loadLanguage(this))
        setupBottomNavigation()
        setupButtonListeners()
        MapUtils.setupMap(binding.mapView, this)
        setupTrackManagement()
        val trackId = intent.getIntExtra("TRACK_ID", -1)
        if (trackId != -1) {
            viewModel.fetchTrackByID(trackId)
        } else {
            viewModel.fetchLastTrack()
        }

        // setupAllTracksStats()

        // TODO maybe only testing:
        viewModel.fetchTrackList()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Check if HomeActivity is in the back stack
        val startMain = Intent(this, HomePageActivity::class.java)
        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(startMain)
        finish()
    }

    // CONTROLS
    private fun setupButtonListeners() {
        binding.buttonLoadPrev.setOnClickListener {
            val success = viewModel.fetchCycle(false)
            success.let {
                if (!it) {
                    showCycleError()
                }
            }
        }
        binding.buttonLoadNext.setOnClickListener {
            val success = viewModel.fetchCycle(true)
            success.let {
                if (!it) {
                    showCycleError()
                }
            }
        }
        binding.buttonDeleteLoadedTrack.setOnClickListener {
            DialogUtils.showConfirmationDialog(
                this,
                getString(R.string.confirmation_dialog_track_deletion_msg),
                onPositiveAction = {
                    viewModel.deleteLoadedTrack()
                }
            )
        }

        binding.buttonRestoreView.setOnClickListener {
            MapUtils.centerAndZoomOverCurrentRoute(binding.mapView, true, checkMapState = true, largePadding = true)
        }
    }

    private fun showCycleError() {
        Toast.makeText(this, getString(R.string.track_cant_cycle_load), Toast.LENGTH_SHORT).show()
    }

    // TRACK LOAD MANAGEMENT
    private var numOfTracks = 0
    private fun setupTrackManagement() {
        viewModel.fetchTrackIDs()
        viewModel.fetchTrackCount()
        viewModel.totalNumberOfTracks.observe(this) {
            it?.let {
                numOfTracks = it
                val formattedNumOfTracks = getString(R.string.num_of_tracks) + " " + numOfTracks
                binding.numberOfTracks.text = formattedNumOfTracks
            }
        }
        viewModel.trackLoader.observe(this) { track ->
            if (track != null) {
                track.trackData?.let {
                    MapUtils.loadCompleteRoute(binding.mapView, it)
                    MapUtils.centerAndZoomOverCurrentRoute(binding.mapView, true, checkMapState = true, largePadding = true)
                    updateTelemetryFromTrack(track)
                }
                // Toast.makeText(this, getString(R.string.track_load_success), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.track_load_failure), Toast.LENGTH_SHORT).show()
                MapUtils.clearMapAndRefresh(binding.mapView)
                clearTelemetry()
            }
        }
        viewModel.deleteSuccess.observe(this) { success ->
            success?.let {
                if (it) {
                    viewModel.fetchLastTrack()
                    viewModel.fetchTrackList()
                    Toast.makeText(this, getString(R.string.track_delete_success), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.track_delete_failure), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /*
    private fun setupAllTracksStats() {
        viewModel.trackListLoader.observe(this) { trackList ->
            if (trackList.isNotEmpty()) {
                binding.allTracksDistance.text = TrackAnalysis.getTracksTotalDistanceInKm(trackList)
                binding.allTracksDuration.text = TrackAnalysis.getTracksTotalDuration(trackList)
                binding.allTracksAverageSpeed.text =
                    TrackAnalysis.getTracksAverageSpeedInKmh(trackList)
                binding.allTracksMaxSpeed.text = TrackAnalysis.getTracksMaxDuration(trackList)
            }
        }
    }*/

    private fun updateTelemetryFromTrack(loadedTrack: Track) {
        val trackName = loadedTrack.name
        if (trackName.isNotBlank()) {
            binding.trackName.text = loadedTrack.name
            binding.trackName.visibility = View.VISIBLE
        } else {
            binding.trackName.visibility = View.GONE
        }

        binding.trackDate.visibility = View.VISIBLE
        binding.trackDate.text = loadedTrack.getFormattedDateTime()

        binding.duration.visibility = View.VISIBLE
        binding.duration.text = StyleUtils.getStyledDuration(loadedTrack.getFormattedDuration(true), false)

        binding.distance.visibility = View.VISIBLE
        binding.distance.text = StyleUtils.getStyledMagnitude(loadedTrack.getFormattedDistanceInKm())

        val formattedAveSpeed = loadedTrack.getFormattedAverageSpeedInKmh()
        val labelledAveSpeed = formattedAveSpeed + " " + getString(R.string.ave_speed_label)
        binding.maxSpeed.visibility = View.VISIBLE
        binding.aveSpeed.text = StyleUtils.getStyledMagnitude(labelledAveSpeed)

        val formattedMaxSpeed = loadedTrack.getFormattedMaxSpeedInKmh()
        val labelledMaxSpeed = formattedMaxSpeed + " " + getString(R.string.max_speed_label)
        binding.maxSpeed.visibility = View.VISIBLE
        binding.maxSpeed.text = StyleUtils.getStyledMagnitude(labelledMaxSpeed)

        val formattedMaxAltitude = loadedTrack.getFormattedMaxAltitudInMeters()
        val labelledMaxAltitude = formattedMaxAltitude + " " + getString(R.string.max_alt_label)
        binding.maxAltitude.visibility = View.VISIBLE
        binding.maxAltitude.text = StyleUtils.getStyledMagnitude(labelledMaxAltitude)

        val elevationInMeters = loadedTrack.trackData?.let { calculateElevationInMeters(it) }
        if (elevationInMeters != null) {
            val formattedElevation = TrackAnalysis.formatAltitudeInMeters(elevationInMeters)
            val labelledElevation = formattedElevation + " " + getString(R.string.elevation_label)
            binding.elevation.text = StyleUtils.getStyledMagnitude(labelledElevation)
            binding.elevation.visibility = View.VISIBLE
        } else {
            binding.elevation.visibility = View.GONE
        }
    }

    private fun clearTelemetry() {
        binding.trackName.text = getString(R.string.no_tracks_available)
        binding.trackName.visibility = View.VISIBLE
        binding.trackDate.visibility = View.GONE
        binding.duration.visibility = View.GONE
        binding.distance.visibility = View.GONE
        binding.aveSpeed.visibility = View.GONE
        binding.maxSpeed.visibility = View.GONE
        binding.maxAltitude.visibility = View.GONE
        binding.elevation.visibility = View.GONE
    }

    // RECOMMENDED FOR OSMDROID IMPLEMENTATION
    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDetach()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause() // Ensures any changes or state are paused
    }

}