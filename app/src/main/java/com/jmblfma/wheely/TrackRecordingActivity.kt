package com.jmblfma.wheely

import SaveTrackFragment
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.jmblfma.wheely.databinding.TrackRecordingBinding
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.services.TrackingService
import com.jmblfma.wheely.utils.MapUtils
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.utils.TrackAnalysis
import com.jmblfma.wheely.utils.TrackRecordingState
import com.jmblfma.wheely.viewmodels.TrackRecordingViewModel
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent

class TrackRecordingActivity : NavigationMenuActivity() {
    private lateinit var binding: TrackRecordingBinding
    private val viewModel: TrackRecordingViewModel by viewModels()

    companion object {
        private const val LIGHTWEIGHT_LOCATION_REFRESH_RATE = 2000
        private const val ACCURACY_CHECK_ENABLED = false
    }

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

    override fun onResume() {
        super.onResume()
        autoDetectAndRestoreState() // only applies when ACTIVE_RECORDING (more info. inside the function)
        binding.mapView.onResume() // ensures map tiles and other resources are refreshed
        IntentFilter().apply {
            addAction(TrackingService.SERVICE_STARTED)
            addAction(TrackingService.SERVICE_STOPPED)
            addAction(TrackingService.SERVICE_ACC_MET)
        }.also { filter ->
            LocalBroadcastManager.getInstance(this).registerReceiver(serviceStateReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause() // Ensures any changes or state are paused
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDetach()
        viewModel.setUIState(null) // necessary for autoDetectAndRestoreState()
        stopLightweightLocationUpdates()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceStateReceiver)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Check if HomeActivity is in the back stack
        val startMain = Intent(this, HomePageActivity::class.java)
        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(startMain)
        finish()
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
            Toast.makeText(
                this,
                getString(R.string.track_recording_reset),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.mapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                if (isTriggeredByUser) { // this is a flag to prevent that automatic animations trigger disabling autocentering
                    isAutoCenterEnabled = false
                }
                return false
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                return false
            } // UNUSED
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
                    MapUtils.animateToLocation(binding.mapView, it, true, true)
                    Log.d("MapDebug", "button(restoreAndFollow) executed")
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
                MapUtils.animateToLocation(binding.mapView, it, false, true)
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
                        if (ACCURACY_CHECK_ENABLED) {
                            Toast.makeText(
                                this,
                                getString(R.string.accuracy_threshold_waiting),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        binding.buttonStartRec.isEnabled = false
                        binding.buttonStopRec.isEnabled = false
                        binding.buttonSaveTrack.isEnabled = false
                        binding.buttonDiscardTrack.isEnabled = false
                        binding.buttonCenterAndFollow.isEnabled = true
                        binding.restoreAndFollow.isEnabled = true
                    }

                    TrackRecordingState.READY_FOR_RECORDING -> {
                        if (ACCURACY_CHECK_ENABLED) {
                            Toast.makeText(
                                this,
                                getString(R.string.accuracy_threshold_reached),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Toast.makeText(this, getString(R.string.acc_check_disabled), Toast.LENGTH_SHORT).show()
                        }
                        binding.buttonStartRec.isEnabled = true
                        binding.buttonStopRec.isEnabled = false
                        binding.buttonSaveTrack.isEnabled = false
                        binding.buttonDiscardTrack.isEnabled = false
                        binding.buttonCenterAndFollow.isEnabled = true
                        binding.restoreAndFollow.isEnabled = true
                    }

                    TrackRecordingState.ACTIVE_RECORDING_REQUESTED -> {

                        Toast.makeText(
                            this,
                            getString(R.string.active_tracking_requested),
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.buttonStartRec.isEnabled = false
                        binding.buttonStopRec.isEnabled = false
                        binding.buttonSaveTrack.isEnabled = false
                        binding.buttonDiscardTrack.isEnabled = false
                        binding.buttonCenterAndFollow.isEnabled = false
                        binding.restoreAndFollow.isEnabled = false
                    }

                    TrackRecordingState.ACTIVE_RECORDING -> {
                        stopLightweightLocationUpdates()
                        resetAutoCenterState()
                        binding.buttonStartRec.isEnabled = false
                        binding.buttonStopRec.isEnabled = true
                        binding.buttonSaveTrack.isEnabled = false
                        binding.buttonDiscardTrack.isEnabled = false
                        binding.buttonCenterAndFollow.isEnabled = true
                        binding.restoreAndFollow.isEnabled = true
                    }

                    TrackRecordingState.SAVING_MODE -> {
                        if (setMapToSaveMode()) {
                            binding.buttonStartRec.isEnabled = false
                            binding.buttonStopRec.isEnabled = false
                            binding.buttonSaveTrack.isEnabled = true
                            binding.buttonDiscardTrack.isEnabled = true
                            binding.buttonCenterAndFollow.isEnabled = false
                            binding.restoreAndFollow.isEnabled = true
                        } else {
                            binding.buttonStartRec.isEnabled = false
                            binding.buttonStopRec.isEnabled = false
                            binding.buttonSaveTrack.isEnabled = false
                            binding.buttonDiscardTrack.isEnabled = false
                            binding.buttonCenterAndFollow.isEnabled = false
                            binding.restoreAndFollow.isEnabled = false
                            Toast.makeText(
                                this,
                                getString(R.string.tracking_didnt_save_anything),
                                Toast.LENGTH_SHORT
                            ).show()
                            restartTrackingPostSaving()
                        }
                    }
                }
            }
        }
    }
    private var safeLoadMode = false
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
                MapUtils.isMapReady = true
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

    @SuppressLint("MissingPermission")
    private fun startLightweightLocationUpdates() {
        MapUtils.clearMapAndRefresh(binding.mapView)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.Builder(LIGHTWEIGHT_LOCATION_REFRESH_RATE.toLong())
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { newLocation ->
                    lastTrackPoint = TrackPoint.mapToTrackPoint(newLocation)
                    MapUtils.updateLocationMarker(
                        binding.mapView,
                        lastTrackPoint!!,
                        isAutoCenterEnabled,
                        false
                    )
                    MapUtils.drawAccuracyCircle(binding.mapView, newLocation)

                    if (TrackingService.isAccuracyEnough(newLocation) || !ACCURACY_CHECK_ENABLED) {
                        viewModel.setUIState(TrackRecordingState.READY_FOR_RECORDING)
                    }
                    binding.accuracyThreshold.text =
                        TrackingService.isAccuracyEnough(newLocation).toString()
                }
            }
        }
        fusedLocationClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLightweightLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    // ACTIVE TRACK RECORDING SERVICE AND SETUP
    private fun setupTrackRecordingLogic() {
        viewModel.trackPointsLiveData.observe(this) { trackPoints ->
            // && !restoringPreviousState* see onResume()
            if (trackPoints.isNotEmpty() && viewModel.getUIState() != TrackRecordingState.SAVING_MODE && !loadingSomethingOntoMap) {
                lastTrackPoint = trackPoints.last()
                MapUtils.liveRouteUpdate(binding.mapView, lastTrackPoint!!, true)
                MapUtils.updateLocationMarker(
                    binding.mapView,
                    lastTrackPoint!!,
                    isAutoCenterEnabled,
                    true
                )
                updateLiveTelemetry(trackPoints)
            }
        }
        // PASSIVE TELEMETRY
        // updates time counter only when the track recording actually starts
        // when the service gets its first location update
        viewModel.elapsedTime.observe(this) { elapsedTime ->
            binding.elapsedTime.text = TrackAnalysis.formatDurationFromMillis(elapsedTime)
        }
    }

    private fun startTracking() {
        Log.d("TESTING", "TrackingService Start Requested")
        Intent(this, TrackingService::class.java).also {
            startForegroundService(it)
        }
        viewModel.setUIState(TrackRecordingState.ACTIVE_RECORDING_REQUESTED)
    }

    private fun stopTracking() {
        Log.d("TESTING", "TrackingService Stop Requested")
        viewModel.setUIState(TrackRecordingState.SAVING_MODE)
        Intent(this, TrackingService::class.java).also { intent ->
            stopService(intent)
        }
    }

    private val serviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                TrackingService.SERVICE_STARTED -> {
                    Log.d("TrackingService", "TrackingService/ RECEIVER/ LAUNCHED")
                    viewModel.setUIState(TrackRecordingState.ACTIVE_RECORDING)
                }

                TrackingService.SERVICE_STOPPED -> {
                    Log.d("TrackingService", "TrackingService/ RECEIVER/ STOPPED")
                    Toast.makeText(
                        this@TrackRecordingActivity,
                        getString(R.string.active_tracking_stopped),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TrackingService.SERVICE_ACC_MET -> {
                    Log.d("TrackingService", "TrackingService/ RECEIVER/ ACC_MET")
                    Toast.makeText(
                        this@TrackRecordingActivity,
                        getString(R.string.active_tracking_ongoing),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // TELEMETRY
    private fun updateLiveTelemetry(trackPoints: List<TrackPoint>) {
        binding.speed.text = TrackAnalysis.formatSpeedInKmh(trackPoints.last().speed.toDouble())
        val currentDistanceInMeters = TrackAnalysis.calculateTotalDistanceInMeters(trackPoints)
        binding.distance.text = TrackAnalysis.formatDistanceInKm(currentDistanceInMeters)
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
            if (it.isNotEmpty()) {
                Toast.makeText(this, getString(R.string.vehicles_loaded), Toast.LENGTH_SHORT).show()
                val dialog = SaveTrackFragment()
                dialog.show(supportFragmentManager, "SaveTrackDialog")
            } else {
                Toast.makeText(this, getString(R.string.no_vehicles_available), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // monitors the success of the track saving operation
        viewModel.saveSuccess.observe(this) { success ->
            success?.let {
                if (it) {
                    restartTrackingPostSaving()
                    Toast.makeText(this, getString(R.string.track_save_success), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this, getString(R.string.track_save_failure), Toast.LENGTH_SHORT)
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

    private fun setMapToSaveMode(): Boolean {
        viewModel.getTrackPoints()?.let { trackPoints ->
            if (trackPoints.isNotEmpty()) {
                loadingSomethingOntoMap = true
                MapUtils.loadCompleteRoute(binding.mapView, trackPoints, true)
                MapUtils.centerAndZoomOverCurrentRoute(binding.mapView, checkMapState = true)
                updateLiveTelemetry(trackPoints)
                loadingSomethingOntoMap = false
                return true
            }
        }
        return false
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