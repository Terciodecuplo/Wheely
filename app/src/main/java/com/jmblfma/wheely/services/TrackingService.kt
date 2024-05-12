package com.jmblfma.wheely.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.jmblfma.wheely.R
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.repository.TrackDataRepository
import com.jmblfma.wheely.utils.TrackAnalysis

// https://developer.android.com/develop/sensors-and-location/location/request-updates

class TrackingService : Service(), SensorEventListener {
    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
        // Refresh rate for location requests in ms
        const val LOCATION_REFRESH_RATE = 1000
        const val ACCURACY_THRESHOLD = 30
        @Volatile var isRunning = false
        @Volatile var enoughAccuracyForTracking = false

        fun isAccuracyEnough(location: Location): Boolean {
            return location.accuracy <= ACCURACY_THRESHOLD
        }
    }

    // SERVICE CFG AND STATES
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // TODO ? request location access at runtime?
        // https://developer.android.com/develop/sensors-and-location/location/permissions
        startLocationUpdates()
        isRunning = true
        // sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // startSensorUpdates()
        Log.d("TrackingService"," onCreate()")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        return START_NOT_STICKY
    }

    private var notificationBuilder: Notification.Builder? = null
    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun startForegroundService() {
        notificationBuilder = Notification.Builder(this, CHANNEL_ID)
            // TODO move to strings
            .setContentTitle("Wheely - Route in progress")
            .setContentText("Location is being tracked")
            .setSmallIcon(R.drawable.ic_tracker)
        val notification: Notification = notificationBuilder!!.build()
        startForeground(1, notification)
    }
    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
        stopTimer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        // stops location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)
        // sensorManager.unregisterListener(this)
        Log.d("TrackingService"," onDestroy()")
    }

    // BINDER SYSTEM - CURRENTLY UNUSED
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    // LOCATION TRACKING
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isPaused: Boolean = false

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        // gets default base location before starting periodic updates
        // added temp. annotation as it requires permissions management
        /* fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                TODO("Not yet implemented")
                // can be null
                // build track point?
            } */

        // (1) sets the location request settings before initializing periodic updates
        val locationRequest = LocationRequest.Builder(LOCATION_REFRESH_RATE.toLong())
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        // (2) specifies what should happen when the location updates received *
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                if (!isPaused) {
                    for (location in locationResult.locations) {
                        if (location.accuracy <= ACCURACY_THRESHOLD) {
                            enoughAccuracyForTracking = true
                            if (startTime == null) {
                                startTime = location.time
                                startTimer()
                            }
                            repository.addTrackPoint(buildTrackPoint(location))
                            Log.d("TrackingService", "TrackingService/ Location Processed!")
                        } else {
                            Log.d("TrackingService", "TrackingService/ Location DISCARDED - LACKING ACCURACY")
                            enoughAccuracyForTracking = false
                        }
                    }
                }
            }
        }

        // after the 'configuration' is set (1 & 2), we can then start requesting updates:
        // requests updates; when updates are available, uses the code in (2) to handle them
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun pauseTracking() {
        isPaused = true
    }
    fun resumeTracking() {
        isPaused = false
    }

    // TRACKPOINTS BUILDING
    private val repository = TrackDataRepository.sharedInstance
    private fun buildTrackPoint(location: Location): TrackPoint {
        // TODO merge data with other sensors here?
        return TrackPoint.mapToTrackPoint(location)
    }

    // TIMER SYSTEM
    private var startTime: Long? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    fun startTimer() {
        runnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - startTime!!
                repository.updateElapsedTime(elapsedMillis)
                updateNotification(elapsedMillis)
                handler.postDelayed(this, 1000L) // Schedule the runnable to run again after 1 second
            }
        }
        handler.post(runnable)
    }
    private fun updateNotification(elapsedTime: Long) {
        val updatedNotification = notificationBuilder
            ?.setContentText("Elapsed time: ${TrackAnalysis.formatDurationFromMillis(elapsedTime)}")
            ?.build()
        startForeground(1, updatedNotification)
    }
    private fun stopTimer() {
        handler.removeCallbacks(runnable)
        startTime = null
    }

    // TODO SENSOR SYSTEM
    private lateinit var sensorManager: SensorManager
    private fun startSensorUpdates() {
        TODO("Not yet implemented")
        // sensor listeners init
    }

    override fun onSensorChanged(event: SensorEvent?) {
        TODO("Not yet implemented")
        // pitch calculation
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
        // if needed
    }
}