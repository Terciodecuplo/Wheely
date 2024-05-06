package com.jmblfma.wheely

import SaveTrackFragment
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import com.jmblfma.wheely.databinding.TrackRecordingBinding
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.services.TrackingService
import com.jmblfma.wheely.utils.MapUtils
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.utils.TrackRecordingState
import com.jmblfma.wheely.utils.formatTime
import com.jmblfma.wheely.viewmodels.TrackRecordingViewModel

class TrackRecordingActivity : NavigationMenuActivity() {
    private lateinit var binding: TrackRecordingBinding
    private val viewModel: TrackRecordingViewModel by viewModels()
    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_record
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TrackRecordingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()
        setupButtonListeners()

        MapUtils.setupMap(binding.mapView, this)
        setupUIManagement()

        setupTrackRecordingLogic()
        setupTrackSavingLogic()
        autoDetectAndRestoreState()
    }

    // UI MGMT AND CONTROLS
    private fun setupButtonListeners() {
        binding.buttonStartRec.setOnClickListener {
            startTracking()
        }
        binding.buttonStopRec.setOnClickListener {
            stopTracking()
        }
        binding.buttonSaveTrack.setOnClickListener {
            val dialog = SaveTrackFragment()
            dialog.show(supportFragmentManager, "SaveTrackDialog")
            viewModel.fetchCurrentVehicleList()
        }
        binding.buttonDiscardTrack.setOnClickListener {
            viewModel.discardTrack()
            restartTrackingPostSaving()
        }
        binding.toggleAutoCenter.isChecked = isAutoCenterEnabled
        binding.toggleAutoCenter.setOnCheckedChangeListener { _, isChecked ->
            isAutoCenterEnabled = isChecked
            if (isAutoCenterEnabled) {
                lastTrackPoint?.let {
                    MapUtils.updateLocationMarker(binding.mapView, it, true, isBearingEnabled)
                }
            }
        }
        // binding.buttonTest2.setOnClickListener {}
    }
    private fun setupUIManagement() {
        viewModel.trackRecordingState.observe(this) {
            Log.d("TESTING","TrackRecording/ setupUIManagement/ UIState: ${it.name}")
            when (it) {
                TrackRecordingState.WAITING_FOR_ACCURACY -> {
                    startLightweightLocationUpdates()
                    Snackbar.make(binding.root, getString(R.string.accuracy_threshold_waiting), Snackbar.LENGTH_SHORT).show()
                    binding.buttonStartRec.isEnabled = false
                    binding.buttonStopRec.isEnabled = false
                    binding.buttonSaveTrack.isEnabled = false
                    binding.buttonDiscardTrack.isEnabled = false
                }
                TrackRecordingState.READY_FOR_RECORDING -> {
                    Snackbar.make(binding.root, getString(R.string.accuracy_threshold_reached), Snackbar.LENGTH_SHORT).show()
                    binding.buttonStartRec.isEnabled = true
                    binding.buttonStopRec.isEnabled = false
                    binding.buttonSaveTrack.isEnabled = false
                    binding.buttonDiscardTrack.isEnabled = false
                }
                TrackRecordingState.ACTIVE_RECORDING_REQUESTED -> {
                    Snackbar.make(binding.root, getString(R.string.active_tracking_launching), Snackbar.LENGTH_SHORT).show()
                    stopLightweightLocationUpdates()
                    binding.buttonStartRec.isEnabled = false
                    binding.buttonStopRec.isEnabled = false
                    binding.buttonSaveTrack.isEnabled = false
                    binding.buttonDiscardTrack.isEnabled = false
                }
                TrackRecordingState.ACTIVE_RECORDING -> {
                    Snackbar.make(binding.root, getString(R.string.active_tracking_ongoing), Snackbar.LENGTH_SHORT).show()
                    if (!restoringPreviousState) {
                        stopLightweightLocationUpdates()
                        restoringPreviousState = false
                    }
                    binding.buttonStartRec.isEnabled = false
                    binding.buttonStopRec.isEnabled = true
                    binding.buttonSaveTrack.isEnabled = false
                    binding.buttonDiscardTrack.isEnabled = false
                }
                TrackRecordingState.SAVING_MODE -> {
                    if (!restoringPreviousState) {
                        setMapToSaveMode()
                        restoringPreviousState = false
                    }
                    binding.buttonStartRec.isEnabled = false
                    binding.buttonStopRec.isEnabled = false
                    binding.buttonSaveTrack.isEnabled = true
                    binding.buttonDiscardTrack.isEnabled = true
                }
            }
        }
    }
    private fun autoDetectAndRestoreState() {
        val isTrackingServiceRunning = TrackingService.isRunning
        if (!isTrackingServiceRunning) {
            if (!viewModel.areThereTrackPointsInTheBuffer()) {
                // INITIAL STATE:
                // not currently tracking & tracking has not started yet (no trackpoints created)
                viewModel.setUIState(TrackRecordingState.WAITING_FOR_ACCURACY, true)
            } else {
                // STATE: not currently tracking but with trackpoints in the buffer
                // this means that the user left the activity after
                // stopping the tracking but without saving -> restore saving mode
                viewModel.setUIState(TrackRecordingState.SAVING_MODE, true)
                restoreMapState(TrackRecordingState.SAVING_MODE)
            }
        } else {
            viewModel.setUIState(TrackRecordingState.ACTIVE_RECORDING, true)
            restoreMapState(TrackRecordingState.ACTIVE_RECORDING)
        }
    }
    private var restoringPreviousState: Boolean = false
    private fun restoreMapState(uiState: TrackRecordingState) {
        restoringPreviousState = true
        binding.mapView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // remove the listener immediately to prevent multiple calls
                binding.mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                when (uiState) {
                    TrackRecordingState.ACTIVE_RECORDING -> {
                        restoreLiveRoute()
                    }
                    TrackRecordingState.SAVING_MODE -> {
                        setMapToSaveMode()
                    }
                    else -> {}
                }
                restoringPreviousState = false
            }
        })
    }

    // GENERAL LOCATION CFG
    private var isAutoCenterEnabled = true
    private var isBearingEnabled = true
    private var lastTrackPoint: TrackPoint? = null

    // PRE-SERVICE LAUNCH LIGHTWEIGHT LOCATION FETCH
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private val LOCATION_REFRESH_RATE = 2000
    @SuppressLint("MissingPermission")
    private fun startLightweightLocationUpdates() {
        MapUtils.clearMapAndRefresh(binding.mapView)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.Builder(LOCATION_REFRESH_RATE.toLong())
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { newLocation ->
                    lastTrackPoint = TrackPoint.mapToTrackPoint(newLocation)
                    MapUtils.updateLocationMarker(binding.mapView, lastTrackPoint!!, isAutoCenterEnabled, false)
                    MapUtils.drawAccuracyCircle(binding.mapView, newLocation)
                    if (TrackingService.isAccuracyEnough(newLocation)) {
                        viewModel.setUIState(TrackRecordingState.READY_FOR_RECORDING)
                    }
                    binding.accuracyThreshold.text = TrackingService.isAccuracyEnough(newLocation).toString()
                }
            }
        }
        fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
    private fun stopLightweightLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    // ACTIVE TRACK RECORDING SERVICE AND SETUP
    private fun setupTrackRecordingLogic() {
        viewModel.trackPointsLiveData.observe(this) { trackPoints ->
            if (trackPoints.isNotEmpty() && viewModel.getUIState() != TrackRecordingState.SAVING_MODE) {
                viewModel.setUIState(TrackRecordingState.ACTIVE_RECORDING)
                Log.d("TESTING","TrackRecording/ in LIVE ROUTE")
                val lastNewTrackPoint = trackPoints.last()
                MapUtils.liveRouteUpdate(binding.mapView, lastNewTrackPoint)
                MapUtils.updateLocationMarker(binding.mapView, lastNewTrackPoint, isAutoCenterEnabled, isBearingEnabled)
                updateLiveTelemetry(trackPoints)
            }
        }
        // PASSIVE TELEMETRY
        // updates time counter only when the track recording actually starts
        // when the service gets its first location update
        viewModel.elapsedTime.observe(this) { elapsedTime ->
            binding.elapsedTime.text = formatTime(elapsedTime)
        }
    }
    private fun startTracking() {
        Log.d("TESTING","TrackingService Start Requested")
        stopLightweightLocationUpdates()
        Intent(this, TrackingService::class.java).also {
            startForegroundService(it)
        }
        viewModel.setUIState(TrackRecordingState.ACTIVE_RECORDING_REQUESTED)
    }
    private fun stopTracking() {
        Log.d("TESTING","TrackingService Stop Requested")
        viewModel.setUIState(TrackRecordingState.SAVING_MODE)
        Intent(this, TrackingService::class.java).also { intent ->
            stopService(intent)
        }
        Snackbar.make(binding.root, getString(R.string.active_tracking_stopping), Snackbar.LENGTH_SHORT).show()
    }
    private fun restoreLiveRoute() {
        viewModel.getTrackPoints()?.let { trackPoints ->
            trackPoints.forEach {
                MapUtils.liveRouteUpdate(binding.mapView, it)
            }
            updateLiveTelemetry(trackPoints)
        }
    }

    // TELEMETRY
    private fun updateLiveTelemetry(trackPoints: List<TrackPoint>) {
        val currentSpeedInMs = trackPoints.last().speed.toDouble()
        val currentDistanceInMeters = Track.calculateTotalDistanceInMeters(trackPoints)
        binding.speed.text = Track.formatSpeedInKmh(currentSpeedInMs)
        binding.distance.text = Track.formatDistanceInKm(currentDistanceInMeters)
        binding.accuracyThreshold.text = TrackingService.enoughAccuracyForTracking.toString()
    }
    private fun clearTelemetry() {
        val standByText = getString(R.string.telemetry_standby_placeholder)
        binding.elapsedTime.text = standByText
        binding.speed.text = standByText
        binding.distance.text = standByText
        binding.accuracyThreshold.text = standByText
    }

    // TRACK SAVING LOGIC
    private fun setupTrackSavingLogic() {
        // prepares the vehicles so that SaveTrackFragment can populate the spinner
        viewModel.loadedVehicles.observe(this) {
            Log.d("TESTING", "TrackRecordingActivity/ loadedVehicles: ${it.isEmpty()}")
            if (it.isEmpty()) {
                Snackbar.make(binding.root, getString(R.string.no_vehicles_available), Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, getString(R.string.vehicles_loaded), Snackbar.LENGTH_SHORT).show()
            }
        }

        // monitors the success of the track saving operation
        viewModel.saveSuccess.observe(this) { success ->
            success?.let {
                if (it) {
                    restartTrackingPostSaving()
                    Snackbar.make(binding.root, getString(R.string.track_save_success), Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, getString(R.string.track_save_failure), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun restartTrackingPostSaving() {
        MapUtils.clearMapAndRefresh(binding.mapView)
        clearTelemetry()
        viewModel.setUIState(TrackRecordingState.WAITING_FOR_ACCURACY)
    }
    private fun setMapToSaveMode() {
        viewModel.getTrackPoints()?.let { trackPoints ->
            MapUtils.loadCompleteRoute(binding.mapView, trackPoints, true)
            updateLiveTelemetry(trackPoints)
        }
    }

    // RECOMMENDED FOR OSMDROID IMPLEMENTATION
    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDetach()
        stopLightweightLocationUpdates()
    }
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume() // Ensures map tiles and other resources are refreshed
    }
    override fun onPause() {
        super.onPause()
        binding.mapView.onPause() // Ensures any changes or state are paused
    }

    // LEGACY
    // TRACKING SERVICE BINDING (service connection setup) - UNUSED
    /*
    private lateinit var trackingServiceBind: TrackingService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TrackingService.LocalBinder
            trackingServiceBind = binder.getService()
        }
        override fun onServiceDisconnected(arg0: ComponentName) { }
    }*/

    // bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
    // unbindService(serviceConnection)
}