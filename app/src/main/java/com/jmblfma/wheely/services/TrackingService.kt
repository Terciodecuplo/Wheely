package com.jmblfma.wheely.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.repository.TrackPointsRepositoryImpl

// https://developer.android.com/develop/sensors-and-location/location/request-updates

class TrackingService : Service(), SensorEventListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var sensorManager: SensorManager
    // Refresh rate for location requests in ms
    private val locationRefreshRate = 1000
    private val repository = TrackPointsRepositoryImpl.instance


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationTest","TrackingService onStartCommand received!")
        // initialize location updates and potentially other setup operations here
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()

        return START_NOT_STICKY // ensures service doesn't restart automatically if killed
    }

    /*override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        startLocationUpdates()
        // startSensorUpdates()
        // TODO ? request location access at runtime?
        // https://developer.android.com/develop/sensors-and-location/location/permissions
        Log.d("LocationTest","TrackingService Launched!")
    }*/

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
        val locationRequest = LocationRequest.Builder(locationRefreshRate.toLong())
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
        // stops location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)
    }
}