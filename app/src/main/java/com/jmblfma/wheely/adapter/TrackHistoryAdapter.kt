package com.jmblfma.wheely.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jmblfma.wheely.R
import com.jmblfma.wheely.model.Track
import java.time.format.DateTimeFormatter
import java.util.Locale

class TrackHistoryAdapter(val trackHistoryList: List<Track>, var context: Context) :
    RecyclerView.Adapter<TrackHistoryAdapter.MyHolder>() {

    class MyHolder(item: View) : RecyclerView.ViewHolder(item) {
        var trackTitle: TextView
        var trackDate: TextView
        var trackTime: TextView
        var trackDuration: TextView
        var trackDistance: TextView
        var trackAvgSpeed: TextView
        var trackVehicle: TextView
        var trackPreview: ImageView


        init {
            trackTitle = item.findViewById(R.id.routeTitle_text)
            trackDate = item.findViewById(R.id.routeDate_text)
            trackTime = item.findViewById(R.id.routeTime_text)
            trackDuration = item.findViewById(R.id.duration_text)
            trackDistance = item.findViewById(R.id.distance_text)
            trackAvgSpeed = item.findViewById(R.id.avgSpeed_text)
            trackVehicle = item.findViewById(R.id.vehicleName_text)
            trackPreview = item.findViewById(R.id.trackPreview_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view: View
        view = LayoutInflater.from(context).inflate(R.layout.track_history_recycler, parent, false)
        return MyHolder(view)
    }

    override fun getItemCount(): Int {
        return trackHistoryList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val dateFormatter =
            DateTimeFormatter.ofPattern("dd MMMM yyyy - HH:mm:ss", Locale.getDefault())
        var history = trackHistoryList[position]
        Log.d("TESTING","The ADAPTER has: ${history.name}")
        holder.trackTitle.text = history.name
        holder.trackDate.text = history.getFormattedDate()
        holder.trackTime.text = history.getFormattedTime(true)
        /*holder.trackDuration.text = history.trackSummary.elapsedTime.toString()
        holder.trackDistance.text = history.trackSummary.distanceTraveled.toString()
        holder.trackAvgSpeed.text = history.trackSummary.averageSpeed.toString()
        holder.trackVehicle.text = holder.itemView.context.getString(
            R.string.vehicle_display_format,
            history.vehicleUsed.brand,
            history.vehicleUsed.model
        )*/
        holder.trackPreview.setImageResource(R.drawable.route_example)
    }
}