package com.jmblfma.wheely.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jmblfma.wheely.R
import com.jmblfma.wheely.model.Track

class TrackHistoryAdapter(val trackHistoryList: ArrayList<Track>, var context: Context) :
    RecyclerView.Adapter<TrackHistoryAdapter.MyHolder>() {

    class MyHolder(item: View) : RecyclerView.ViewHolder(item) {
        var trackTitle: TextView
        var trackLocation: TextView
        var trackDate: TextView
        var trackDuration: TextView
        var trackDistance: TextView
        var trackAvgSpeed: TextView
        var trackVehicle: TextView
        var trackPreview: ImageView


        init {
            trackTitle = item.findViewById(R.id.routeTitle_text)
            trackLocation = item.findViewById(R.id.routeLocation_text)
            trackDate = item.findViewById(R.id.routeDate_text)
            trackDuration = item.findViewById(R.id.duration_text)
            trackDistance = item.findViewById(R.id.distance_text)
            trackAvgSpeed = item.findViewById(R.id.avgSpeed_text)
            trackVehicle = item.findViewById(R.id.vehicle_text)
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
        var history = trackHistoryList[position]
        holder.trackTitle.text = history.name
        holder.trackLocation.text = history.generalLocation
        holder.trackDate =
        holder.trackPreview.setImageResource(R.drawable.route_example)
    }
}
