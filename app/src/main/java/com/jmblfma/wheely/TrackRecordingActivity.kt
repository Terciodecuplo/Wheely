package com.jmblfma.wheely

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.jmblfma.wheely.databinding.TrackRecordingBinding
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.services.TrackingService
import com.jmblfma.wheely.utils.MapUtils
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.utils.formatTime
import com.jmblfma.wheely.viewmodels.TrackDataViewModel

class TrackRecordingActivity : NavigationMenuActivity() {
    private lateinit var binding: TrackRecordingBinding
    private val viewModel: TrackDataViewModel by viewModels()
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

        if (!TrackingService.isRunning) {
            startLightweightLocationUpdates()
            updateButtonStates(false)
        } else {
            updateButtonStates(true)
        }
        setupTrackRecording()
    }

    // CONTROLS
    private fun setupButtonListeners() {
        binding.buttonStartRec.setOnClickListener {
            startTracking()
        }

        binding.buttonStopRec.setOnClickListener {
            stopTracking()
        }

        binding.buttonSaveTrack.setOnClickListener {
            Log.d("TESTING","UI/ viewModel.saveCurrentTrack()")
            viewModel.saveCurrentTrack()
        }


        binding.buttonClearUI.setOnClickListener {
            resetUI()
        }

        binding.toggleAutoCenter.isChecked = isAutoCenterEnabled
        binding.toggleAutoCenter.setOnCheckedChangeListener { _, isChecked ->
            isAutoCenterEnabled = isChecked
            if (isAutoCenterEnabled) {
                MapUtils.updateLocationMarker(binding.mapView, lastLocation, isAutoCenterEnabled, isBearingEnabled)
            }
        }

        binding.buttonTest2.setOnClickListener {
            Log.d("TESTING","UI/ buttonTest2: UNASSIGNED")
        }
    }
    private fun updateButtonStates(isTrackingActive: Boolean, isAccuracyEnough: Boolean = false) {
        Log.d("TESTING","UI/ updateButtonStates: $isTrackingActive")
        binding.buttonStartRec.isEnabled = !(isTrackingActive || !isAccuracyEnough)
        binding.buttonSaveTrack.isEnabled = !isTrackingActive
        binding.buttonClearUI.isEnabled = !isTrackingActive
        binding.buttonStopRec.isEnabled = isTrackingActive
    }

    // GENERAL LOCATION CFG
    private var isAutoCenterEnabled = true
    private var isBearingEnabled = true
    private lateinit var lastLocation: Location

    // PRE-SERVICE LAUNCH LIGHTWEIGHT LOCATION FETCH
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val LOCATION_REFRESH_RATE = 2000
    @SuppressLint("MissingPermission")
    private fun startLightweightLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.Builder(LOCATION_REFRESH_RATE.toLong())
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        Log.d("TESTING","UI/ locationCallback received")
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let {
                    Log.d("TESTING","UI/ locationCallback received")
                    lastLocation = it
                    MapUtils.updateLocationMarker(binding.mapView, it, isAutoCenterEnabled, false)
                    MapUtils.drawAccuracyCircle(binding.mapView, it)
                    accuracyThresholdCheck = TrackingService.isAccuracyEnough(it)
                    updateButtonStates(false, accuracyThresholdCheck)
                    binding.accuracyThreshold.text = TrackingService.isAccuracyEnough(it).toString()
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
    private fun stopLightweightLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // ACTIVE TRACK RECORDING SERVICE AND SETUP
    private var accuracyThresholdCheck = false
    private fun setupTrackRecording() {
        viewModel.trackPoints.observe(this) { trackPoints ->
            Log.d("TESTING","UI/ viewModel OBSERVED!")

            MapUtils.addOrUpdateRouteOnMap(binding.mapView, trackPoints, true)
            lastLocation = MapUtils.mapToLocation(trackPoints.last())
            MapUtils.updateLocationMarker(binding.mapView, lastLocation, isAutoCenterEnabled, isBearingEnabled)
            MapUtils.drawAccuracyCircle(binding.mapView, lastLocation)
            updateLiveTelemetry(trackPoints)
        }
        // PASSIVE TELEMETRY
        // updates time counter only when the route tracking actually starts
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
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        updateButtonStates(true)
    }
    private fun stopTracking() {
        Log.d("TESTING","TrackingService Stop Requested")
        Intent(this, TrackingService::class.java).also { intent ->
            stopService(intent)
            unbindService(serviceConnection)
        }
        updateButtonStates(false)
    }

    // TRACKING SERVICE BINDING (service connection setup) - UNUSED
    private lateinit var trackingServiceBind: TrackingService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TrackingService.LocalBinder
            trackingServiceBind = binder.getService()
        }
        override fun onServiceDisconnected(arg0: ComponentName) { }
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
        binding.elapsedTime.text = "---"
        binding.speed.text = "---"
        binding.distance.text = "---"
        binding.accuracyThreshold.text = "---"
    }

    // OTHER FUNCTIONS
    private fun resetUI() {
        MapUtils.clearMap(binding.mapView)
        clearTelemetry()
        Log.d("TESTING","UI/ CLEARED UI & MAP OVERLAYS!")
    }
}