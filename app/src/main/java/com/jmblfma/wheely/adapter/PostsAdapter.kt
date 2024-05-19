package com.jmblfma.wheely.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jmblfma.wheely.R
import com.jmblfma.wheely.TrackViewerActivity
import com.jmblfma.wheely.data.Difficulty
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.utils.MapUtils
import org.osmdroid.views.MapView

class PostsAdapter(private val trackList: List<Track>, private val usersById: Map<Int, User>) :
    RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userProfileImage: ImageView = view.findViewById(R.id.user_profile_img)
        val userName: TextView = view.findViewById(R.id.user_name)
        val trackDateAndTime: TextView = view.findViewById(R.id.track_date_and_time)
        val trackName: TextView = view.findViewById(R.id.track_name)
        val trackDuration: TextView = view.findViewById(R.id.track_duration)
        val trackDistance: TextView = view.findViewById(R.id.track_distance)
        val trackSpeed: TextView = view.findViewById(R.id.track_speed)
        val trackDifficulty: TextView = view.findViewById(R.id.track_difficulty)
        val trackDifficultyTitle: TextView = view.findViewById(R.id.track_difficulty_title)
        val trackDescription: TextView = view.findViewById(R.id.track_description)
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
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    view.context.startActivity(intent)
                }
            }
            mapPreview.addOnFirstLayoutListener { v, left, top, right, bottom -> // remove the listener to prevent multiple calls
                MapUtils.centerAndZoomOverCurrentRoute(mapPreview, false, false)
            }
        }

        fun bind(track: Track, user: User?) {
            setPostImage(user)
            userName.text = user?.nickname ?: "UNKNOWN USER"
            trackName.text = track.name

            userName.text = user?.nickname ?: "UNKNOWN USER"
            if (track.name.isNotBlank()) {
                trackName.text = track.name
                trackName.visibility = View.VISIBLE
            } else {
                trackName.visibility = View.GONE
            }

            trackDateAndTime.text = track.getFormattedDateTime()

            trackDuration.text = track.getFormattedDuration()
            trackDistance.text  = track.getFormattedDistanceInKm()
            trackSpeed.text = track.getFormattedAverageSpeedInKmh()
            if (track.difficultyValue != Difficulty.UNKNOWN) {
                trackDifficulty.text = track.difficultyValue.toString()
                trackDifficulty.visibility = View.VISIBLE
                trackDifficultyTitle.visibility = View.VISIBLE
            } else {
                trackDifficulty.visibility = View.GONE
                trackDifficultyTitle.visibility = View.GONE
            }

            if (!track.description.isNullOrBlank()) {
                trackDescription.text = track.description;
                trackDescription.visibility = View.VISIBLE;
            } else {
                trackDescription.visibility = View.GONE;
            }


            track.trackData?.let {
                MapUtils.loadRoutePreview(mapPreview, it)
            }
            selectedTrackId = track.trackId
        }

        private fun setPostImage(user: User?) {
            if (user != null) {
                Glide.with(userProfileImage.context)
                    .load(user.profileImage)
                    .into(userProfileImage)
            }
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
