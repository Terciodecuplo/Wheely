package com.jmblfma.wheely.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
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
import com.jmblfma.wheely.utils.TrackAnalysis

class TrackHistoryAdapter(
    private val trackHistoryList: List<Track>,
    private val vehicleList: List<Vehicle>,
    var context: Context,
    private val itemClickListener: OnTrackItemClickListener
) :
    RecyclerView.Adapter<TrackHistoryAdapter.MyHolder>() {
    interface OnTrackItemClickListener {
        fun onTrackItemClick(track: Track)
    }

    class MyHolder(
        view: View,
        private val trackHistoryList: List<Track>,
        private val itemClickListener: OnTrackItemClickListener
    ) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        var trackTitle: TextView
        var trackDate: TextView
        var trackDuration: TextView
        var trackDistance: TextView
        var trackAvgSpeed: TextView
        var vehicleImage: ImageView


        init {
            trackTitle = view.findViewById(R.id.routeTitle_text)
            trackDate = view.findViewById(R.id.route_date_text)
            trackDuration = view.findViewById(R.id.duration_text)
            trackDistance = view.findViewById(R.id.distance_text)
            trackAvgSpeed = view.findViewById(R.id.avgSpeed_text)
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

        holder.trackTitle.text = titleTextFormatter(holder, track, vehicle)
        holder.trackDate.text = track.getFormattedDateTime()

        holder.trackDuration.text = track.getFormattedDuration()
        holder.trackDistance.text = track.getFormattedDistanceInKm()
        holder.trackAvgSpeed.text = track.getFormattedAverageSpeedInKmh()

        if (vehicle != null) {
            setVehicleImage(holder.vehicleImage, vehicle.image)
        }
    }

    private fun titleTextFormatter(
        holder: MyHolder,
        track: Track,
        vehicle: Vehicle?
    ): SpannableString {
        var formattedText = ""
        val trackName = if (track.name.isNullOrEmpty()) context.getString(R.string.default_track_name) else track.name
        if(vehicle != null) {
            formattedText = String.format(
                holder.trackTitle.context.getString(R.string.vehicle_used_in_track),
                trackName,
                vehicle.brand,
                vehicle.model
            )
        } else {
            formattedText = trackName
        }

        val spannable = SpannableString(formattedText)

        val trackNameLength = trackName.length
        val trackTitleMotoInfoLength = formattedText.length
        Log.d(
            "TESTING",
            "TrackNameLength => $trackNameLength  |||| Rest => $trackTitleMotoInfoLength"
        )


        val textSize24sp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            24f,
            holder.itemView.resources.displayMetrics
        ).toInt()

        val textSize16sp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            16f,
            holder.itemView.resources.displayMetrics
        ).toInt()

        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            trackNameLength,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            AbsoluteSizeSpan(textSize24sp),
            0,
            trackNameLength,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            StyleSpan(Typeface.NORMAL),
            trackNameLength,
            trackTitleMotoInfoLength,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            AbsoluteSizeSpan(textSize16sp),
            trackNameLength,
            trackTitleMotoInfoLength,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }

    private fun setVehicleImage(imageView: ImageView, imagePath: String?) {
        if (imagePath.isNullOrEmpty()) {
            Log.d("VEHICLE", "Image path = $imagePath")
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