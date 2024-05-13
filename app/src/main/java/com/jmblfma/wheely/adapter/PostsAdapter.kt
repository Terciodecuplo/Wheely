package com.jmblfma.wheely.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jmblfma.wheely.R
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.utils.MapUtils
import org.osmdroid.views.MapView

class PostsAdapter(private val trackList: List<Track>, private val usersById: Map<Int, User>) :
    RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userProfileImage: ImageView = view.findViewById(R.id.user_profile_img)
        val userName: TextView = view.findViewById(R.id.user_name)
        val trackDateAndLocation: TextView = view.findViewById(R.id.track_date_and_location)
        val trackName: TextView = view.findViewById(R.id.track_name)
        val trackStats: TextView = view.findViewById(R.id.track_stats)
        val mapPreview: MapView = view.findViewById(R.id.map_preview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_recycler, parent, false)
        return PostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentTrack = trackList[position]
        val currentUser = usersById[currentTrack.drivenByUserId]
        holder.userProfileImage.setImageResource(R.drawable.user_default_pic)
        holder.userName.text = currentUser?.nickname ?: "UNKNOWN USER"

        holder.trackName.text = currentTrack.name
        val trackDateAndTime = currentTrack.getFormattedDateTime()
        holder.trackDateAndLocation.text = trackDateAndTime // TODO add general location when implemented

        val trackDuration = currentTrack.getFormattedDuration()
        val trackDistance = currentTrack.getFormattedDistanceInKm()
        val trackAveSpeed = currentTrack.getFormattedAverageSpeedInKmh()
        holder.trackStats.text = "Time: $trackDuration · Distance: $trackDistance · Speed: $trackAveSpeed"

        currentTrack.trackData?.let {
            MapUtils.setupMapRoutePreview(holder.mapPreview, holder.itemView.context, it)
        }
    }

    override fun getItemCount(): Int = trackList.size
}
