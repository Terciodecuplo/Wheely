package com.jmblfma.wheely

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
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
        setupTrackLoading()
    }

    // CONTROLS
    private fun setupButtonListeners() {
        binding.buttonLoadPrev.setOnClickListener {
            Log.d("TESTING","UI/TrackViewer/ LOAD PREVIOUS")
            cycleTrack(false)
        }

        binding.buttonLoadNext.setOnClickListener {
            Log.d("TESTING","UI/TrackViewer/ LOAD NEXT")
            cycleTrack(true)
        }

        binding.buttonClearUI.setOnClickListener {

        }
    }

    private fun setupTrackLoading() {
        // updates the local variable lastTrackId so it is possible to cycle
        // between all tracks without going over the limit
        viewModel.fetchTrackCount()
        viewModel.totalNumberOfTracks.observe(this) { totalNumberOfTracks ->
            lastTrackId = totalNumberOfTracks
            Log.d("TESTING","UI/TrackViewer/ lastTrackId: $lastTrackId")
        }

        viewModel.trackLoader.observe(this) { track ->
            if (track != null) {
                Log.d("TESTING", "UI/TrackViewer/ TRACK LOADED: $track.trackId")
                binding.mapView.overlays.clear()
                // TODO CALL MapsUtil updatePathOnMap(track.trackData, true)
                MapUtils.addOrUpdateRouteOnMap(binding.mapView, track.trackData, false)
                updateTelemetryFromTrack(track)
            } else {
                // TODO Handle the case where track is null (perhaps show an error or a message)
            }
        }
    }

    private var trackIdPointer = 0 // trackIDs start at 1
    private var lastTrackId = 0
    private val firstTrackId = 1
    private fun cycleTrack(forward: Boolean) {
        if (forward) {
            trackIdPointer++
        } else {
            trackIdPointer--
        }
        // Allows to keep cycling after getting to the last track (jumps to first one again)
        if (trackIdPointer < firstTrackId) {
            trackIdPointer = lastTrackId
        } else if (trackIdPointer > lastTrackId) {
            trackIdPointer = firstTrackId
        }
        Log.d("TESTING","UI/TrackViewer/ viewModel.fetchTrackByID($trackIdPointer)")
        viewModel.fetchTrackByID(trackIdPointer)
    }

    private fun updateTelemetryFromTrack(loadedTrack: Track) {
        binding.elapsedTime.text = loadedTrack.getFormattedDuration()
        binding.speed.text = loadedTrack.getFormattedAverageSpeedInKmh()
        binding.distance.text = loadedTrack.getFormattedDistanceInKm()
        binding.trackID.text = String.format("%d / %d", loadedTrack.trackId, lastTrackId)
    }

}