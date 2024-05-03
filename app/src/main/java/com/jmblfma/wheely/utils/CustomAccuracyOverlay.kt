import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.cos
import kotlin.math.pow

class CustomAccuracyOverlay(private var location: Location? = null, var accuracyThreshold: Float = 10f) : Overlay() {
    private val accuracyPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        location?.let { loc ->
            if (loc.hasAccuracy()) {
                val geoPoint = GeoPoint(loc.latitude, loc.longitude)
                val projectedPoint = mapView.projection.toPixels(geoPoint, null)

                // Calculate meters per pixel
                val metersPerPixel = metersPerPixel(mapView.zoomLevelDouble, loc.latitude)
                val radiusInPixels = (loc.accuracy / metersPerPixel).toFloat()

                // Set the paint color based on accuracy
                if (loc.accuracy >= accuracyThreshold) {
                    accuracyPaint.color = Color.argb(50, 255, 50, 50)  // Semi-transparent red
                } else {
                    accuracyPaint.color = Color.argb(50, 50, 50, 255)  // Semi-transparent blue
                }
                canvas.drawCircle(projectedPoint.x.toFloat(), projectedPoint.y.toFloat(), radiusInPixels, accuracyPaint)
            }
        }
    }

    fun updateLocation(location: Location) {
        this.location = location
    }

    private fun metersPerPixel(zoomLevel: Double, latitude: Double): Double {
        val equatorLength = 40075016.686  // in meters
        val widthInPixels = 256 * 2.0.pow(zoomLevel)
        val radians = latitude * Math.PI / 180
        val metersPerPixel = equatorLength * cos(radians) / widthInPixels
        return metersPerPixel
    }
}
