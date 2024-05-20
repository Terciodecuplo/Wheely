package com.jmblfma.wheely.utils

import CustomAccuracyOverlay
import android.content.Context
import android.location.Location
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
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
    // TODO zoom/ make dynamic depending on speed?
    // TODO zoom/ animate zoom changes between levels
    // CONFIG
    const val MIN_ZOOM_LEVEL = 5.0
    const val MAX_ZOOM_LEVEL =
        21.0 // sensible: 20; switch to 30.0 for debugging (might generate crashes)
    const val ACTIVE_ZOOM_LEVEL = 20.0
    const val DEFAULT_ZOOM_LEVEL = 18.0
    private val ROUTE_ACTIVE_COLOR = ContextCompat.getColor(MyApp.applicationContext(), R.color.activeRouteColor)
    private val ROUTE_LOAD_COLOR = ContextCompat.getColor(MyApp.applicationContext(), R.color.loadedRouteColor)
    private val ROUTE_PENDING_SAVE_COLOR = ContextCompat.getColor(MyApp.applicationContext(), R.color.unsavedRouteColor)
    fun setupMap(mapView: MapView, context: Context) {
        isMapReady = false
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(false)
        mapView.minZoomLevel = MIN_ZOOM_LEVEL
        mapView.maxZoomLevel = MAX_ZOOM_LEVEL
        mapView.controller.setZoom(DEFAULT_ZOOM_LEVEL)
        clearMapAndRefresh(mapView)
    }

    fun setupMapRoutePreview(mapView: MapView, context: Context) {
        isMapReady = false
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(false)
        mapView.setBuiltInZoomControls(false)
        mapView.minZoomLevel = MIN_ZOOM_LEVEL
        mapView.maxZoomLevel = MAX_ZOOM_LEVEL
        val routePreview = Polyline()
        routePreview.color = ROUTE_LOAD_COLOR
        mapView.overlays.add(routePreview)
    }

    fun loadRoutePreview(mapView: MapView, trackPoints: List<TrackPoint>) {
        val routePreview = mapView.overlays.filterIsInstance<Polyline>().firstOrNull()
        routePreview?.let {
            val geoPoints = trackPoints.map { GeoPoint(it.latitude, it.longitude) }
            routePreview.setPoints(geoPoints)
            setUpRouteEndpointsMarker(mapView, geoPoints.first(), true)
            setUpRouteEndpointsMarker(mapView, geoPoints.last(), false)
            mapView.invalidate()
        }
    }

    fun clearMapAndRefresh(mapView: MapView) {
        mapView.overlays.clear()
        mapView.invalidate()
    }

    fun drawAccuracyCircle(mapView: MapView, location: Location) {
        if (mapView.overlays.none { it is CustomAccuracyOverlay }) {
            val accuracyOverlay =
                CustomAccuracyOverlay(accuracyThreshold = TrackingService.ACCURACY_THRESHOLD.toFloat())
            mapView.overlays.add(accuracyOverlay)
            mapView.invalidate()
        }
        val accuracyOverlay =
            mapView.overlays.filterIsInstance<CustomAccuracyOverlay>().firstOrNull()
        accuracyOverlay?.updateLocation(location)
        mapView.invalidate()
    }

    // ROUTE UPDATES AND LOADING
    fun liveRouteUpdate(
        mapView: MapView,
        newTrackPoint: TrackPoint,
        addStartMarker: Boolean = false
    ) {
        if (mapView.overlays.none { it is Polyline }) {
            clearMapAndRefresh(mapView)
            val newLiveRoute = Polyline()
            newLiveRoute.color = ROUTE_ACTIVE_COLOR
            mapView.overlays.add(newLiveRoute)
            if (addStartMarker) {
                setUpRouteEndpointsMarker(
                    mapView,
                    GeoPoint(newTrackPoint.latitude, newTrackPoint.longitude),
                    true
                )
            }
            // setZoom(mapView, ACTIVE_ZOOM_LEVEL) // sets default zoom value to start with
        }

        val currentRoute = mapView.overlays.filterIsInstance<Polyline>().firstOrNull()
        currentRoute?.let {
            it.addPoint(GeoPoint(newTrackPoint.latitude, newTrackPoint.longitude))
            mapView.invalidate()
        }
    }

    fun clearCurrentRoute(mapView: MapView) {
        val currentRoute = mapView.overlays.filterIsInstance<Polyline>().firstOrNull()
        currentRoute?.let {
            mapView.overlays.remove(currentRoute)
        }
        mapView.invalidate()
    }

    private fun setUpRouteEndpointsMarker(mapView: MapView, where: GeoPoint, isStart: Boolean) {
        val newMarker = Marker(mapView).apply {
            position = where
            icon = if (isStart) {
                MyApp.applicationContext().getDrawable(R.drawable.ic_start_marker)
            } else {
                MyApp.applicationContext().getDrawable(R.drawable.ic_end_marker)
            }
            // TODO extract to resources (if used at some point; not shown now)
            // title = if (isStart) "Start" else "End"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(newMarker)
    }

    fun loadCompleteRoute(
        mapView: MapView,
        trackPoints: List<TrackPoint>,
        unsaved: Boolean = false
    ) {
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
        setUpRouteEndpointsMarker(mapView, geoPoints.first(), true)
        setUpRouteEndpointsMarker(mapView, geoPoints.last(), false)
    }

    // LOCATION MARKER SETUP AND MGMT
    const val LOC_MARKER_TITLE =
        "CurrentLocationMarker" // TODO implement custom marker class to better filter by subtype


    fun updateLocationMarker(
        mapView: MapView,
        trackPoint: TrackPoint,
        autoCenter: Boolean = true,
        isLiveRoute: Boolean = false
    ) {
        var firstUpdate = false
        if (mapView.overlays.none { it is Marker && it.title == LOC_MARKER_TITLE }) {
            // creates the marker if it doesn't exist
            Marker(mapView).apply {
                if (isLiveRoute) {
                    icon = MyApp.applicationContext().getDrawable(R.drawable.ic_direction_arrow)
                    // setZoom(mapView, ACTIVE_ZOOM_LEVEL) // sets default zoom value to start with
                } else {
                    icon = MyApp.applicationContext().getDrawable(R.drawable.ic_location_pointer)
                    // setZoom(mapView, DEFAULT_ZOOM_LEVEL)
                }
                title = LOC_MARKER_TITLE
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                mapView.overlays.add(this)
                firstUpdate = true
            }
        }

        val currentMarker =
            mapView.overlays.filterIsInstance<Marker>().firstOrNull { it.title == LOC_MARKER_TITLE }
        currentMarker?.let { marker ->
            val geoPoint = GeoPoint(trackPoint.latitude, trackPoint.longitude)
            marker.position = geoPoint
            if (isLiveRoute && trackPoint.hasBearing()) {
                marker.rotation = -trackPoint.bearing!!
            }
            mapView.invalidate()
            if (autoCenter) {
                if (firstUpdate) {
                   if (isLiveRoute) {
                       animateToLocation(mapView, trackPoint, true, false, ACTIVE_ZOOM_LEVEL)
                   } else {
                       animateToLocation(mapView, trackPoint, true, false, DEFAULT_ZOOM_LEVEL)
                   }
                    firstUpdate = false
                } else {
                    animateToLocation(mapView, trackPoint, false, true)
                }
            }
        }
    }

    // TODO refine speed logic to prevent some weird response when:
    // explicit call from centering button, autoCenter = true and then it gets called again from liveTracking before finishing the first animation
    // 200L works well to hide the problem almost completely; but we might want to do it slower without the problem as well
    const val ANIMATION_TIME_FAST = 200L
    fun animateToLocation(
        mapView: MapView,
        where: TrackPoint,
        restoreZoom: Boolean = false,
        fastAnimation: Boolean = false,
        zoomLevel: Double = ACTIVE_ZOOM_LEVEL
    ) {
        val pSpeed =
            if (fastAnimation) ANIMATION_TIME_FAST else null // "null" sets the default animation speed (see Implementation)
        val pZoom =
            if (restoreZoom) zoomLevel else mapView.zoomLevelDouble // applies current zoom value
        mapView.controller.animateTo(GeoPoint(where.latitude, where.longitude), pZoom, pSpeed)
    }

    fun setZoom(mapView: MapView, zoomLevel: Double = ACTIVE_ZOOM_LEVEL) {
        mapView.controller.setZoom(zoomLevel)
    }

    const val BOUNDING_BOX_PADDING = 100
    const val BOUNDING_BOX_PADDING_LARGE = 350
    var isMapReady = false
    // TODO think about the use of this in viewer vs recording vs post adapter a bit more
    // it works fine in this state but might be able to refactor the system
    fun centerAndZoomOverCurrentRoute(
        mapView: MapView,
        animated: Boolean = true,
        checkMapState: Boolean = true,
        largePadding: Boolean = false
    ) {
        val selectedPadding = if (largePadding) BOUNDING_BOX_PADDING_LARGE else BOUNDING_BOX_PADDING
        val currentRoute = mapView.overlays.filterIsInstance<Polyline>().firstOrNull()
        currentRoute?.let {
            val boundingBox = currentRoute.bounds
            if (checkMapState && !isMapReady) {
                val observer = mapView.viewTreeObserver
                observer.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        // remove the listener to prevent multiple calls
                        mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        isMapReady = true
                        mapView.zoomToBoundingBox(boundingBox, animated, selectedPadding)
                    }
                })
            } else {
                mapView.zoomToBoundingBox(boundingBox, animated, selectedPadding)
            }
        }
    }
}
