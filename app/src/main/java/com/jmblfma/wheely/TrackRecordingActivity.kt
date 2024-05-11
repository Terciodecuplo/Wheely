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
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent

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
        MapUtils.setupMap(binding.mapView, this)
        setupUIManagement()
        setupButtonAndMapsListeners()
        setupTrackRecordingLogic()
        setupTrackSavingLogic()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDetach()
        viewModel.setUIState(null) // necessary for autoDetectAndRestoreState()
        stopLightweightLocationUpdates()
    }
    override fun onResume() {
        super.onResume()
        autoDetectAndRestoreState() // only applies when ACTIVE_RECORDING (more info. inside the function)
        binding.mapView.onResume() // ensures map tiles and other resources are refreshed
    }
    override fun onPause() {
        super.onPause()
        binding.mapView.onPause() // Ensures any changes or state are paused
    }

    // UI MGMT AND CONTROLS
    @SuppressLint("ClickableViewAccessibility") // applies to "binding.mapView.setOnTouchListener"
    private fun setupButtonAndMapsListeners() {
        binding.buttonStartRec.setOnClickListener {
            startTracking()
        }
        binding.buttonStopRec.setOnClickListener {
            stopTracking()
        }
        binding.buttonSaveTrack.setOnClickListener {
            viewModel.fetchCurrentVehicleList()
            // the saving screen will be on launch from the observer after the vehicles have been loaded
            // see setupTrackSavingLogic()
        }
        binding.buttonDiscardTrack.setOnClickListener {
            viewModel.discardTrack()
            restartTrackingPostSaving()
        }

        binding.mapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                if (isTriggeredByUser) { // this is a flag to prevent that automatic animations trigger disabling autocentering
                    isAutoCenterEnabled = false
                }
                return false
            }
            override fun onZoom(event: ZoomEvent?): Boolean { return false } // UNUSED
        })
        binding.mapView.setOnTouchListener { _, event ->
            // set the flag on any user interaction with the map
            isTriggeredByUser = true
            // return false to allow the map to handle all default interactions (panning, zooming, etc.)
            false
        }
        binding.restoreAndFollow.setOnClickListener {
            resetAutoCenterState()
            if (viewModel.getUIState() == TrackRecordingState.SAVING_MODE) {
                MapUtils.centerAndZoomOverCurrentRoute(binding.mapView)
            } else { // restores it only if there is an active location being provided
                lastTrackPoint?.let {
                    // forces immediate recenter & sets zoom to default mode
                    MapUtils.animateToLocation(binding.mapView, it, true)
                    MapUtils.setZoom(binding.mapView)
                }
            }
        }
        // TODO maybe delete this function?
        // normally we you want to recenter you also want to restore the zoom level?
        binding.buttonCenterAndFollow.setOnClickListener {
            resetAutoCenterState()
            // disabled in SAVING MODE
            lastTrackPoint?.let {
                // forces immediate recenter but doesn't change the zoom level
                MapUtils.animateToLocation(binding.mapView, it, true)
            }
        }
        // TODO add button to zoom out to current live track using MapUtils.centerAndZoomOverCurrentRoute?

    }
    var isTriggeredByUser: Boolean = false
    private fun resetAutoCenterState() {
        isTriggeredByUser = false
        isAutoCenterEnabled = true
    }
    private fun setupUIManagement() {
        viewModel.trackRecordingState.observe(this) {
            it?.let {
                // Log.d("TEST2","TrackRecording/ setupUIManagement/ UIState: ${it.name}")
                when (it) {
                    TrackRecordingState.WAITING_FOR_ACCURACY -> {
                        resetAutoCenterState()
                        startLightweightLocationUpdates()
                        Snackbar.make(binding.root, getString(R.string.accuracy_threshold_waiting), Snackbar.LENGTH_SHORT)
                            .setAction(getString(R.string.snackbar_dismiss)) { }
                            .show()
                        binding.buttonStartRec.isEnabled = false
                        binding.buttonStopRec.isEnabled = false
                        binding.buttonSaveTrack.isEnabled = false
                        binding.buttonDiscardTrack.isEnabled = false
                        binding.buttonCenterAndFollow.isEnabled = true
                        binding.restoreAndFollow.isEnabled = true
                    }
                    TrackRecordingState.READY_FOR_RECORDING -> {
                        Snackbar.make(binding.root, getString(R.string.accuracy_threshold_reached), Snackbar.LENGTH_SHORT)
                            .setAction(getString(R.string.snackbar_dismiss)) { }
                            .show()
                        binding.buttonStartRec.isEnabled = true
                        binding.buttonStopRec.isEnabled = false
                        binding.buttonSaveTrack.isEnabled = false
                        binding.buttonDiscardTrack.isEnabled = false
                        binding.buttonCenterAndFollow.isEnabled = true
                        binding.restoreAndFollow.isEnabled = true
                    }
                    TrackRecordingState.ACTIVE_RECORDING_REQUESTED -> {
                        Snackbar.make(binding.root, getString(R.string.active_tracking_launching), Snackbar.LENGTH_SHORT)
                            .setAction(getString(R.string.snackbar_dismiss)) { }
                            .show()
                        stopLightweightLocationUpdates()
                        binding.buttonStartRec.isEnabled = false
                        binding.buttonStopRec.isEnabled = false
                        binding.buttonSaveTrack.isEnabled = false
                        binding.buttonDiscardTrack.isEnabled = false
                        binding.buttonCenterAndFollow.isEnabled = false
                        binding.restoreAndFollow.isEnabled = false
                    }
                    TrackRecordingState.ACTIVE_RECORDING -> {
                        resetAutoCenterState()
                        Snackbar.make(binding.root, getString(R.string.active_tracking_ongoing), Snackbar.LENGTH_SHORT)
                            .setAction(getString(R.string.snackbar_dismiss)) { }
                            .show()
                        binding.buttonStartRec.isEnabled = false
                        binding.buttonStopRec.isEnabled = true
                        binding.buttonSaveTrack.isEnabled = false
                        binding.buttonDiscardTrack.isEnabled = false
                        binding.buttonCenterAndFollow.isEnabled = true
                        binding.restoreAndFollow.isEnabled = true
                    }
                    TrackRecordingState.SAVING_MODE -> {
                        setMapToSaveMode()
                        binding.buttonStartRec.isEnabled = false
                        binding.buttonStopRec.isEnabled = false
                        binding.buttonSaveTrack.isEnabled = true
                        binding.buttonDiscardTrack.isEnabled = true
                        binding.buttonCenterAndFollow.isEnabled = false
                        binding.restoreAndFollow.isEnabled = true
                    }
                }
            }
        }
    }
    private fun autoDetectAndRestoreState() { // should be called from onResume to deal with ALL cause (first initialization, after onDestroy, after onPause...)
        val isTrackingServiceRunning = TrackingService.isRunning
        val currentUiState = viewModel.getUIState()
        if (currentUiState == null) { // onDestroy to onCreate OR first onCreate (DEFAULT INITIALIZATION)
            if (isTrackingServiceRunning) { // onDestroy when TRACKING -> restore tracking state
                restoreLiveRoute()
                viewModel.setUIState(TrackRecordingState.ACTIVE_RECORDING)
            } else if (viewModel.areThereTrackPointsInTheBuffer()) {  // onDestroy when SAVING -> restore saving state
                viewModel.setUIState(TrackRecordingState.SAVING_MODE)
            } else { // nothing going on previously; DEFAULT INITIALIZATION:
                viewModel.setUIState(TrackRecordingState.WAITING_FOR_ACCURACY)
            }
        } else if (currentUiState == TrackRecordingState.ACTIVE_RECORDING) { // onPause TRACKING to onResume TRACKING
            restoreLiveRoute() // fills 'missing' points between last onPause and onResume
        } else { // onPause when WAITING_FOR_ACCURACY, SAVING_MODE...
            // NOTHING TO RESTORE
            // ...already waiting for accuracy
            // ...already with the map set to saving mode
        }
    }
    // Boolean flag to prevent active tracking to modify route while any other map manipulation is taking place
    private var loadingSomethingOntoMap: Boolean = false
    // prevents crashes for certain uses of MapView when redrawing the different elements on the map
    private fun restoreMapAfterMapViewIsLoaded(uiState: TrackRecordingState) {
        loadingSomethingOntoMap = true
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
                loadingSomethingOntoMap = false
            }
        })
    } // CURRENTLY UNUSED
    // TODO restoreLiveRoute could be integrated into liveRouteUpdate() to eliminate this code from autoDetectAndRestoreState()
    private fun restoreLiveRoute() {
        viewModel.getTrackPoints()?.let { trackPoints ->
            loadingSomethingOntoMap = true
            // because this is going to redraw everything from the start of the route
            // the current route from must be cleared from the map overlays before regenerating it
            // otherwise there would be a straight line drawn from the last trackpoint to the first one after onResume
            MapUtils.clearCurrentRoute(binding.mapView)
            trackPoints.forEach {
                MapUtils.liveRouteUpdate(binding.mapView, it, true)
            }
            loadingSomethingOntoMap = false
            updateLiveTelemetry(trackPoints)
        }
    }

    // GENERAL LOCATION CFG
    private var isAutoCenterEnabled = true
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
            // && !restoringPreviousState* see onResume()
            if (trackPoints.isNotEmpty() && viewModel.getUIState() != TrackRecordingState.SAVING_MODE && !loadingSomethingOntoMap) {
                viewModel.setUIState(TrackRecordingState.ACTIVE_RECORDING)
                lastTrackPoint = trackPoints.last()
                MapUtils.liveRouteUpdate(binding.mapView, lastTrackPoint!!, true)
                MapUtils.updateLocationMarker(binding.mapView, lastTrackPoint!!, isAutoCenterEnabled, true)
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
        Snackbar.make(binding.root, getString(R.string.active_tracking_stopping), Snackbar.LENGTH_SHORT)
            .setAction(getString(R.string.snackbar_dismiss)) { }
            .show()
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
        // launches the saving window after vehicles are fetch when clicking binding.buttonSaveTrack
        // TODO might be moved to onCreate and always fetch this list beforehand?
        viewModel.loadedVehicles.observe(this) {
            Log.d("TEST2", "TrackRecordingActivity/ loadedVehicles: ${it.isEmpty()} / Size: ${it.size}")
            if (it.isNotEmpty()) {
                Snackbar.make(binding.root, getString(R.string.vehicles_loaded), Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.snackbar_dismiss)) { }
                    .show()
                val dialog = SaveTrackFragment()
                dialog.show(supportFragmentManager, "SaveTrackDialog")
            } else {
                Snackbar.make(binding.root, getString(R.string.no_vehicles_available), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.snackbar_dismiss)) { }
                    .show()
            }
        }

        // monitors the success of the track saving operation
        viewModel.saveSuccess.observe(this) { success ->
            success?.let {
                if (it) {
                    restartTrackingPostSaving()
                    Snackbar.make(binding.root, getString(R.string.track_save_success), Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.snackbar_dismiss)) { }
                        .show()
                } else {
                    Snackbar.make(binding.root, getString(R.string.track_save_failure), Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.snackbar_dismiss)) { }
                        .show()
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
            loadingSomethingOntoMap = true
            MapUtils.loadCompleteRoute(binding.mapView, trackPoints, true)
            updateLiveTelemetry(trackPoints)
            loadingSomethingOntoMap = false
        }
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