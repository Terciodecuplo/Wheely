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
    // CONFIG
    const val MIN_ZOOM_LEVEL = 5.0
    const val MAX_ZOOM_LEVEL = 20.0
    // TODO make dynamic depending on speed?
    const val ACTIVE_ZOOM_LEVEL = 18.0
    const val DEFAULT_ZOOM_LEVEL = 19.0
    const val ROUTE_ACTIVE_COLOR = Color.RED
    const val ROUTE_LOAD_COLOR = Color.BLUE
    const val ROUTE_PENDING_SAVE_COLOR = Color.MAGENTA
    fun setupMap(mapView: MapView, context: Context) {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(false)
        mapView.minZoomLevel = MIN_ZOOM_LEVEL
        mapView.maxZoomLevel = MAX_ZOOM_LEVEL
        mapView.controller.setZoom(DEFAULT_ZOOM_LEVEL)
        clearMapAndRefresh(mapView)
    }

    fun clearMapAndRefresh(mapView: MapView) {
        mapView.overlays.clear()
        mapView.invalidate()
    }

    fun drawAccuracyCircle(mapView: MapView, location: Location) {
        if (mapView.overlays.none { it is CustomAccuracyOverlay }) {
            val accuracyOverlay = CustomAccuracyOverlay(accuracyThreshold = TrackingService.ACCURACY_THRESHOLD.toFloat())
            mapView.overlays.add(accuracyOverlay)
            mapView.invalidate()
        }
        val accuracyOverlay = mapView.overlays.filterIsInstance<CustomAccuracyOverlay>().firstOrNull()
        accuracyOverlay?.updateLocation(location)
        mapView.invalidate()
    }

    // ROUTE UPDATES AND LOADING
    fun liveRouteUpdate(mapView: MapView, newTrackPoint: TrackPoint) {
        if (mapView.overlays.none { it is Polyline }) {
            clearMapAndRefresh(mapView)
            val newLiveRoute = Polyline()
            newLiveRoute.color = ROUTE_ACTIVE_COLOR
            mapView.overlays.add(newLiveRoute)
            mapView.controller.setZoom(ACTIVE_ZOOM_LEVEL) // sets default zoom value to start with
        }

        val currentRoute = mapView.overlays.filterIsInstance<Polyline>().firstOrNull()
        currentRoute?.let {
            it.addPoint(GeoPoint(newTrackPoint.latitude,newTrackPoint.longitude))
            mapView.invalidate()
        }
    }

    fun loadCompleteRoute(mapView: MapView, trackPoints: List<TrackPoint>, unsaved: Boolean = false) {
        clearMapAndRefresh(mapView)

        val completeTrackRoute = Polyline()
        if (!unsaved) {
            completeTrackRoute.color = ROUTE_LOAD_COLOR
        } else {
            completeTrackRoute.color = ROUTE_PENDING_SAVE_COLOR
        }
        mapView.overlays.add(completeTrackRoute)
        val geoPoints = trackPoints.map { GeoPoint(it.latitude, it.longitude) }
        completeTrackRoute.setPoints(geoPoints)

        val context = MyApp.applicationContext()
        val startMarker = Marker(mapView).apply {
            position = completeTrackRoute.points.first()
            icon = context.getDrawable(R.drawable.ic_start_marker)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Start"
        }
        val endMarker = Marker(mapView).apply {
            position = completeTrackRoute.points.last()
            icon = context.getDrawable(R.drawable.ic_end_marker)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "End"
        }
        mapView.overlays.add(startMarker)
        mapView.overlays.add(endMarker)
        mapView.invalidate()

        val boundingBox = completeTrackRoute.bounds
        mapView.zoomToBoundingBox(boundingBox, true)

        Log.d("TESTING","MapUtils/ loadCompleteRoute() **DONE**")
    }

    // LOCATION MARKER SETUP AND MGMT
    fun updateLocationMarker(mapView: MapView, trackPoint: TrackPoint, autoCenterAndZoom: Boolean = true, enableBearing: Boolean = false) {
        if (mapView.overlays.none { it is Marker }) {
            // creates the marker if it doesn't exist
            Marker(mapView).apply {
                if (enableBearing) {
                    icon = MyApp.applicationContext().getDrawable(R.drawable.ic_direction_arrow)
                } else {
                    icon = MyApp.applicationContext().getDrawable(R.drawable.ic_location_pointer)
                }
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                mapView.overlays.add(this)
            }
        }

        val currentMarker = mapView.overlays.filterIsInstance<Marker>().firstOrNull()
        currentMarker?.let { marker ->
            val geoPoint = GeoPoint(trackPoint.latitude, trackPoint.longitude)
            marker.position = geoPoint
            if (enableBearing && trackPoint.hasBearing()) {
                marker.rotation = trackPoint.bearing!!
            }
            mapView.invalidate()
            if (autoCenterAndZoom) {
                mapView.controller.animateTo(geoPoint)
                mapView.controller.setZoom(ACTIVE_ZOOM_LEVEL)
            }
        }
    }
}

/* LEGACY DUAL IMPLEMENTATION
fun addOrUpdateRouteOnMap(mapView: MapView, trackPoints: List<TrackPoint>, liveTrack: Boolean = false) {
    // TRUE (no route created) & TRUE (is live track)
    if (mapView.overlays.none { it is Polyline } && liveTrack) {
        clearMap(mapView)  // clear the map if starting a new track or no polyline exists
        val newLiveRoute = Polyline()
        newLiveRoute.color = ROUTE_ACTIVE_COLOR  // Set default color or customize based on conditions
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
        if (liveTrack) { // fits the viewer to the route if a complete track was loaded
            val newTrackPoint = trackPoints.last()
            it.addPoint(GeoPoint(newTrackPoint.latitude,newTrackPoint.longitude))
            mapView.controller.setZoom(ACTIVE_ZOOM_LEVEL)
            // if it is a livetrack centering will be handled separately using updateLocationMarker()
        } else {
            val geoPoints = trackPoints.map { GeoPoint(it.latitude, it.longitude) }
            it.setPoints(geoPoints)
            setMapToFullRoutePreview(mapView, ROUTE_LOAD_COLOR)
        }
        mapView.invalidate()
        Log.d("TESTING","MapUtils/ addOrUpdateRouteOnMap() DONE")
    }
}*/