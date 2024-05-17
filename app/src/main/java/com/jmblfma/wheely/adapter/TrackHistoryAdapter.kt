package com.jmblfma.wheely.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jmblfma.wheely.R
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.Vehicle

class TrackHistoryAdapter(
    private val trackHistoryList: List<Track>,
    private val vehicleList: List<Vehicle>,
    var context: Context,
    private val itemClickListener: OnTrackItemClickListener
) :
    RecyclerView.Adapter<TrackHistoryAdapter.MyHolder>() {
        interface OnTrackItemClickListener{
            fun onTrackItemClick(track : Track)
        }

    class MyHolder(
        view: View,
        private val trackHistoryList: List<Track>,
        private val itemClickListener: OnTrackItemClickListener
    ) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        var trackTitle: TextView
        var trackLocation: TextView
        var trackDate: TextView
        var trackDuration: TextView
        var trackDistance: TextView
        var trackAvgSpeed: TextView
        var trackVehicle: TextView
        var vehicleImage: ImageView


        init {
            trackTitle = view.findViewById(R.id.routeTitle_text)
            trackLocation = view.findViewById(R.id.routeLocation_text)
            trackDate = view.findViewById(R.id.routeDate_text)
            trackDuration = view.findViewById(R.id.duration_text)
            trackDistance = view.findViewById(R.id.distance_text)
            trackAvgSpeed = view.findViewById(R.id.avgSpeed_text)
            trackVehicle = view.findViewById(R.id.vehicleName_text)
            vehicleImage = view.findViewById(R.id.vehicleUsed_image)
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val clickedTrack = trackHistoryList[adapterPosition]
            itemClickListener.onTrackItemClick(clickedTrack)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view: View
        view = LayoutInflater.from(context).inflate(R.layout.track_history_recycler, parent, false)
        return MyHolder(view, trackHistoryList, itemClickListener)
    }

    override fun getItemCount(): Int {
        return trackHistoryList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        var track = trackHistoryList[position]
        var vehicle = vehicleList.find { it.vehicleId == track.vehicleUsedId }
        holder.trackTitle.text = track.name
        holder.trackLocation.text = track.getFormattedDate()
        holder.trackDate.text = track.getFormattedTime(true)
        holder.trackDuration.text = track.getFormattedDuration()
        holder.trackDistance.text = track.getFormattedDistanceInKm()
        holder.trackAvgSpeed.text = track.getFormattedAverageSpeedInKmh()
        if (vehicle != null) {
            holder.trackVehicle.text = holder.itemView.context.getString(
                R.string.vehicle_used_in_track,
                vehicle.brand,
                vehicle.model
            )
        }
        if (vehicle != null) {
            setVehicleImage(holder.vehicleImage, vehicle.image)
        }
    }

    private fun setVehicleImage(imageView: ImageView, imagePath: String?) {
        if (imagePath.isNullOrEmpty()) {
            Log.d("VEHICLE","Image path = $imagePath")
            Glide.with(imageView.context)
                .load(R.drawable.vehicle_placeholder) // Your placeholder drawable
                .into(imageView)
        } else {
            Glide.with(imageView.context)
                .load(imagePath)
                .into(imageView)
        }
    }
}