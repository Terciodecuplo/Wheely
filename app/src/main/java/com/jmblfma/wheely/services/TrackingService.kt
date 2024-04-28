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

// https://developer.android.com/develop/sensors-and-location/location/request-updates

class TrackingService : Service(), SensorEventListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var sensorManager: SensorManager
    // Refresh rate for location requests in ms
    private val repository = TrackDataRepository.sharedInstance

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val LOCATION_REFRESH_RATE = 1000
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // TODO ? request location access at runtime?
        // https://developer.android.com/develop/sensors-and-location/location/permissions
        startLocationUpdates()
        // sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // startSensorUpdates()
        Log.d("TrackingService/"," onCreate()")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        Log.d("TrackingService/"," onStartCommand()")
        return START_NOT_STICKY
    }

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
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking Active")
            .setContentText("Location is being tracked")
            .setSmallIcon(R.drawable.ic_tracker)
            .build()
        startForeground(1, notification)
    }

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
                for (location in locationResult.locations){
                    Log.d("LocationTest","processing LocationResult...")
                    repository.addTrackPoint(buildTrackPoint(location))

                }
                Log.d("LocationTest","LocationCallback Processed!")
            }
        }

        // after the 'configuration' is set (1 & 2), we can then start requesting updates:
        // requests updates; when updates are available, uses the code in (2) to handle them
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
        Log.d("LocationTest","requestLocationUpdates() executed ")
    }

    private fun buildTrackPoint(location: Location): TrackPoint {
        // TODO merge data with other sensors here?

        return TrackPoint(
            location.time,
            location.latitude,
            location.longitude,
            location.altitude,
            location.speed
        )
    }

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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
        // stops location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)
        // sensorManager.unregisterListener(this)
        Log.d("TrackingService/"," onDestroy()")
    }
}