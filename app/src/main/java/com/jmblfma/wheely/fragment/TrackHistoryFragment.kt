package com.jmblfma.wheely.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.jmblfma.wheely.R
import com.jmblfma.wheely.TrackViewerActivity
import com.jmblfma.wheely.VehicleStatsActivity
import com.jmblfma.wheely.adapter.TrackHistoryAdapter
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.viewmodels.UserDataViewModel

class TrackHistoryFragment : Fragment(), TrackHistoryAdapter.OnTrackItemClickListener {

    private lateinit var trackHistoryAdapter: TrackHistoryAdapter
    private val viewModel: UserDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_track_history, container, false)
        viewModel.userTrackList.observe(viewLifecycleOwner) { trackHistory ->
            viewModel.vehicleList.observe(viewLifecycleOwner) { userVehicles ->
                setupRecyclerView(view, trackHistory, userVehicles)
            }
        }
        return view
    }

    private fun setupRecyclerView(
        view: View,
        trackHistoryList: List<Track>,
        userVehicles: List<Vehicle>
    ) {
        trackHistoryAdapter =
            TrackHistoryAdapter(trackHistoryList, userVehicles, requireContext(), this)

        view.findViewById<RecyclerView>(R.id.trackHistory_recycler).apply {
            adapter = trackHistoryAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val appBarLayout = activity?.findViewById<AppBarLayout>(R.id.app_bar_layout)
                    val tabLayout = activity?.findViewById<TabLayout>(R.id.tab_layout)

                    appBarLayout?.let { appBar ->
                        val tabHeight = tabLayout?.height ?: 0

                        // Determine the change we want in translationY
                        val currentTranslation = appBar.translationY
                        val deltaTranslation = currentTranslation - dy

                        // Adjusted minTranslation to keep TabLayout visible
                        val maxTranslation = 0f
                        val minTranslation = 0f

                        // Constrain the new translation within the adjusted bounds
                        val newTranslation = deltaTranslation.coerceIn(minTranslation, maxTranslation)

                        // Set the new translationY
                        appBar.translationY = newTranslation
                    }
                }
            })
            scrollToPosition(trackHistoryList.size - 1)
        }
    }

     override fun onTrackItemClick(track: Track) {
        val intent = Intent(requireContext(), TrackViewerActivity::class.java)
        intent.putExtra("vehicleId", track.trackId)
        Log.d("VEHICLE", "Vehicle selected = $track")
        startActivity(intent)
    }
}
