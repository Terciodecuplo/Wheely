package com.jmblfma.wheely.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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

        // refresh rate for location requests in ms
        const val LOCATION_REFRESH_RATE = 1000
        const val ACCURACY_THRESHOLD = 20 // in meters

        @Volatile
        var isRunning = false

        @Volatile
        var isRecording = false

        @Volatile
        var liveAccuracyThresholdMet = false

        // Broadcasts MSGs
        const val SERVICE_STARTED = "tracking_service_started"
        const val SERVICE_STOPPED = "tracking_service_stopped"
        const val SERVICE_ACC_FAILED = "tracking_service_waiting_acc"
        const val SERVICE_ACC_MET = "tracking_service_acc_met"

        fun isAccuracyEnough(location: Location): Boolean {
            return location.accuracy <= ACCURACY_THRESHOLD
        }
    }

    // SERVICE CFG AND STATES
    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        startLocationUpdates()
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(SERVICE_STARTED))
        // sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // startSensorUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        // makes the service persist even when swiping up the app out of existence
        // requires either to open it again to stop it or forcing close from app settings
        // START_NON_STICKY would kill the service in the first case
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private var notificationBuilder: Notification.Builder? = null
    private fun startForegroundService() {
        notificationBuilder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.tracking_notification_title))
            .setContentText(getString(R.string.tracking_notification_content))
            .setSmallIcon(R.drawable.ic_tracker)
        val notification: Notification = notificationBuilder!!.build()
        startForeground(1, notification)
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(SERVICE_STARTED))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        isRunning = false
        isRecording = false
        liveAccuracyThresholdMet = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(SERVICE_STOPPED))
        // sensorManager.unregisterListener(this)
    }

    // LOCATION TRACKING
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isPaused: Boolean = false

    @SuppressLint("MissingPermission") // permissions are checked before ever launching the service
    private fun startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // (1) sets the location request settings before initializing periodic updates
        val locationRequest = LocationRequest.Builder(LOCATION_REFRESH_RATE.toLong())
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        // (2) specifies what should happen when the location updates are received *
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location.accuracy <= ACCURACY_THRESHOLD) {
                        liveAccuracyThresholdMet = true
                        if (startTime == null) {
                            startTime = location.time
                            startTimer()
                            LocalBroadcastManager.getInstance(this@TrackingService).sendBroadcast(
                                Intent(
                                    SERVICE_ACC_MET
                                )
                            )
                            isRecording = true
                        }
                        repository.addTrackPoint(buildTrackPoint(location))
                    } else {
                        liveAccuracyThresholdMet = false // discarded location
                    }
                }
            }
        }

        // after the 'configuration' is set (1 & 2), we can then start requesting updates:
        // requests updates; when updates are available, uses the code in (2) to handle them
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
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
    private var isTimerRunning = false
    fun startTimer() {
        runnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - startTime!!
                repository.updateElapsedTime(elapsedMillis)
                updateNotification(elapsedMillis)
                handler.postDelayed(
                    this,
                    1000L
                ) // Schedule the runnable to run again after 1 second
            }
        }
        handler.post(runnable)
        isTimerRunning = true
    }

    private fun updateNotification(elapsedTime: Long) {
        val updatedNotification = notificationBuilder
            ?.setContentText(
                getString(R.string.tracking_notification_elapsed_time) + " " + TrackAnalysis.formatDurationFromMillis(
                    elapsedTime
                )
            )
            ?.build()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, updatedNotification)
    }

    private fun stopTimer() {
        if (isTimerRunning) {
            handler.removeCallbacks(runnable)
            startTime = null
            isTimerRunning = false
        }
    }

    // BINDER SYSTEM - CURRENTLY UNUSED
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
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