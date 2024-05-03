package com.jmblfma.wheely.utils

import CustomAccuracyOverlay
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.util.Log
import com.jmblfma.wheely.MyApp
import com.jmblfma.wheely.R
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.services.TrackingService
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

object MapUtils {
    fun setupMap(mapView: MapView, context: Context) {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(17.0)

        val accuracyOverlay = CustomAccuracyOverlay(accuracyThreshold = TrackingService.ACCURACY_THRESHOLD.toFloat())
        mapView.overlays.add(accuracyOverlay)

        Log.d("TESTING", "MapUtils/ setupMap() DONE")
    }

    fun drawAccuracyCircle(mapView: MapView, location: Location) {
        val accuracyOverlay = mapView.overlays.filterIsInstance<CustomAccuracyOverlay>().firstOrNull()
        Log.d("TESTING", "MapUtils/ drawAccuracyCircle() Accuracy: ${location.accuracy}")
        accuracyOverlay?.updateLocation(location)
        mapView.invalidate()
    }

    fun centerMapOnLocation(mapView: MapView, latitude: Double, longitude: Double) {
        val geoPoint = GeoPoint(latitude, longitude)
        mapView.controller.setCenter(geoPoint)
        mapView.controller.setZoom(15.0)
    }

    fun convertTrackPointsToGeoPoints(trackPoints: List<TrackPoint>): List<GeoPoint> {
        return trackPoints.map { GeoPoint(it.latitude, it.longitude) }
    }

    fun addOrUpdateRouteOnMap(mapView: MapView, trackPoints: List<TrackPoint>, liveTrack: Boolean = false) {
        // TRUE (no route created) & TRUE (is live track)
        if (mapView.overlays.none { it is Polyline } && liveTrack) {
            clearMap(mapView)  // clear the map if starting a new track or no polyline exists
            val newLiveRoute = Polyline()
            newLiveRoute.color = Color.RED  // Set default color or customize based on conditions
            mapView.overlays.add(newLiveRoute)
        // FALSE (a route exists) & FALSE (is NOT a live track)
        // a track was loaded before; clear track and load new
        } else if (!liveTrack) {
            clearMap(mapView)
            val completeTrackRoute = Polyline()
            completeTrackRoute.color = Color.RED  // Set default color or customize based on conditions
            mapView.overlays.add(completeTrackRoute)
        }
        // FALSE (a route exists) & TRUE (is a live track)
        // there is an ongoing live track; continue drawing without reloading
        val currentRoute = mapView.overlays.filterIsInstance<Polyline>().firstOrNull()
        currentRoute?.let {
            val geoPoints = trackPoints.map { GeoPoint(it.latitude, it.longitude) }
            it.setPoints(geoPoints)
            if (!liveTrack) { // fits the viewer to the route if a complete track was loaded
                centerMapOnPolyline(mapView, it)
            } else {
                // if it is a livetrack centering will be handled separately using updateLocationMarker()
            }
            mapView.invalidate()
            Log.d("TESTING","MapUtils/ addOrUpdateRouteOnMap() DONE")
        }
    }

    fun mapToLocation(trackPoint: TrackPoint): Location {
        val location = Location("virtual_custom_provider")

        location.time = trackPoint.timestamp
        location.latitude = trackPoint.latitude
        location.longitude = trackPoint.longitude
        location.altitude = trackPoint.altitude
        location.speed = trackPoint.speed
        if (trackPoint.bearing != null) {
            location.bearing = trackPoint.bearing
        }
        Log.d("TESTING", "MapUtils/ mapToLocation: Created Location: lat=${location.latitude}, lon=${location.longitude}, alt=${location.altitude}, speed=${location.speed}, bearing=${location.bearing}")

        return location
    }

    fun centerMapOnPolyline(mapView: MapView, polyline: Polyline) {
        val boundingBox = polyline.bounds // Get the bounding box of the polyline
        // zoom the map to fit the bounding box with some padding
        mapView.zoomToBoundingBox(boundingBox, true, 50)
        Log.d("TESTING","MapUtils/ centerMapOnPolyline() DONE")
    }

    private var userLocationMarker: Marker? = null

    fun initializeMarker(mapView: MapView) {
        val context = MyApp.applicationContext()
        userLocationMarker = Marker(mapView).apply {
            icon = context.getDrawable(R.drawable.ic_location_pointer) // Default marker icon
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            mapView.overlays.add(this)
        }
        Log.d("TESTING", "MapUtils/ initializeMarker() DONE")
    }

    fun updateLocationMarker(mapView: MapView, location: Location, autoCenter: Boolean = true, enableBearing: Boolean = false) {
        val context = MyApp.applicationContext() // Accessing the global application context
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        Log.d("TESTING", "MapUtils/ IN updateLocationMarker()")
        if (userLocationMarker == null) {
            initializeMarker(mapView)
        }

        userLocationMarker?.let { marker ->
            marker.position = geoPoint

            Log.d("TESTING", "MapUtils/ updateLocationMarker() BEARING: ${location.hasBearing()}")
            if (enableBearing && location.hasBearing()) {
                marker.icon = context.getDrawable(R.drawable.ic_direction_arrow)
                marker.rotation = location.bearing
            } else {
                marker.icon = context.getDrawable(R.drawable.ic_location_pointer)
            }

            // Center the map on the new marker position if auto-centering is enabled
            if (autoCenter) {
                mapView.controller.setCenter(geoPoint)
            }

            mapView.invalidate() // Refresh the map to display the updated marker
        }
    }



    fun clearMap(mapView: MapView) {
        mapView.overlays.clear()
        resetLocationMarker()
        mapView.invalidate()
    }

    fun resetLocationMarker() {
        userLocationMarker = null
    }
}