package com.jmblfma.wheely.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jmblfma.wheely.R
import com.jmblfma.wheely.TrackViewerActivity
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
        val mapPreviewHolder: View = view.findViewById<View>(R.id.map_preview_holder)

        private var selectedTrackId: Int? = null

        init {
            MapUtils.setupMapRoutePreview(mapPreview, itemView.context)
            mapPreviewHolder.setOnClickListener {
                selectedTrackId?.let { trackId ->
                    val intent = Intent(view.context, TrackViewerActivity::class.java).apply {
                        putExtra("TRACK_ID", trackId)
                        // TODO review flags a bit more
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    view.context.startActivity(intent)
                }
            }
            mapPreview.addOnFirstLayoutListener { v, left, top, right, bottom -> // remove the listener to prevent multiple calls
                MapUtils.centerAndZoomOverCurrentRoute(mapPreview, false, false)
            }
        }

        fun bind(track: Track, user: User?) {
            userProfileImage.setImageResource(R.drawable.user_default_pic)
            userName.text = user?.nickname ?: "UNKNOWN USER"

            trackName.text = track.name
            val trackDateAndTime = track.getFormattedDateTime()
            trackDateAndLocation.text =
                trackDateAndTime // TODO add general location when implemented

            val trackDuration = track.getFormattedDuration()
            val trackDistance = track.getFormattedDistanceInKm()
            val trackAveSpeed = track.getFormattedAverageSpeedInKmh()
            val statsText =
                "Time: $trackDuration · Distance: $trackDistance · Speed: $trackAveSpeed"
            trackStats.text = statsText

            track.trackData?.let {
                MapUtils.loadRoutePreview(mapPreview, it)
            }
            selectedTrackId = track.trackId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_feed_recycler, parent, false)
        return PostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentTrack = trackList[position]
        val currentUser = usersById[currentTrack.drivenByUserId]
        holder.bind(currentTrack, currentUser)
    }

    override fun getItemCount(): Int = trackList.size
}
