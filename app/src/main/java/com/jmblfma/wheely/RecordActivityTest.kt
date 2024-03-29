package com.jmblfma.wheely

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.jmblfma.wheely.databinding.TestRecordScreenBinding
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.services.TrackingService
import com.jmblfma.wheely.utils.NavigationMenuActivity
import com.jmblfma.wheely.viewmodels.TrackPointsViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class RecordActivityTest : NavigationMenuActivity() {
    private lateinit var binding: TestRecordScreenBinding
    private val viewModel: TrackPointsViewModel by viewModels()
    private lateinit var map: MapView

    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.nav_record
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TestRecordScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()

        // TrackingService TEMP Start System for Testing
        val startRecButton = binding.buttonTest1
        startRecButton.setOnClickListener {
            Log.d("LocationTest","TrackingService Start Requested")
            Intent(this, TrackingService::class.java).also { intent ->
                startService(intent)
            }
        }

        val stopRecButton = binding.buttonTest2
        stopRecButton.setOnClickListener {
            Log.d("LocationTest","TrackingService Stop Requested")
            Intent(this, TrackingService::class.java).also { intent ->
                stopService(intent)
            }
        }


        map = binding.mapView
        setupMapDefaults()
        testMapWithService()
    }
    private fun testMapWithService() {
        viewModel.trackPoints.observe(this) { trackPoints ->
            Log.d("LocationTest","viewModel OBSERVED!")
            updatePathOnMap(trackPoints)

        }
    }

    private fun setupMapDefaults() {
        Log.d("LocationTest","Maps Defaults Set")
        map.setMultiTouchControls(true)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(20)
    }

    private fun updatePathOnMap(trackPoints: List<TrackPoint>) {
        Log.d("LocationTest","PathUpdate!")
        val route = Polyline()
        val geoPoints = viewModel.convertTrackPointsToGeoPoints(trackPoints)
        route.color = Color.RED
        route.setPoints(geoPoints)
        // map.overlays.clear()
        map.overlays.add(route)
        map.controller.setCenter(geoPoints.last())
        map.invalidate()
    }
    fun testMap() {
        val map = binding.mapView
        map.setMultiTouchControls(true)
        map.setTileSource(TileSourceFactory.MAPNIK)

        val currentLocation = GpsMyLocationProvider(this)
        val locationOverlay = MyLocationNewOverlay(currentLocation, map)
        locationOverlay.enableMyLocation()
        map.controller.setZoom(15.0)
        map.overlays.add(locationOverlay)

        locationOverlay.enableFollowLocation()
        //map.controller.setCenter(startPoint)
    }
}