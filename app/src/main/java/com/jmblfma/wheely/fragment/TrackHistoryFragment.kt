package com.jmblfma.wheely.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.jmblfma.wheely.R
import com.jmblfma.wheely.TrackViewerActivity
import com.jmblfma.wheely.adapter.ProfileVehicleListAdapter
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
        return inflater.inflate(R.layout.fragment_track_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel(view)
    }

    private fun observeViewModel(view: View) {
        viewModel.userTrackList.observe(viewLifecycleOwner) { trackHistory ->
            viewModel.vehicleList.observe(viewLifecycleOwner) { userVehicles ->
                if (trackHistory.isNotEmpty() && userVehicles.isNotEmpty()) {
                    setupRecyclerView(view, trackHistory, userVehicles)
                }
            }
        }
    }

    private fun setupRecyclerView(
        view: View,
        trackHistoryList: List<Track>,
        userVehicles: List<Vehicle>
    ) {
        trackHistoryAdapter =
            TrackHistoryAdapter(trackHistoryList.reversed(), userVehicles, requireContext(), this)

        view.findViewById<RecyclerView>(R.id.trackHistory_recycler).apply {
            adapter = trackHistoryAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val appBarLayout = activity?.findViewById<AppBarLayout>(R.id.app_bar_layout)

                    appBarLayout?.let { appBar ->

                        // Determine the change we want in translationY
                        val currentTranslation = appBar.translationY
                        val deltaTranslation = currentTranslation - dy

                        // Adjusted minTranslation to keep TabLayout visible
                        val maxTranslation = 0f
                        val minTranslation = 0f

                        // Constrain the new translation within the adjusted bounds
                        val newTranslation =
                            deltaTranslation.coerceIn(minTranslation, maxTranslation)

                        // Set the new translationY
                        appBar.translationY = newTranslation
                    }
                }
            })
        }
    }

    override fun onTrackItemClick(track: Track) {
        val intent = Intent(requireContext(), TrackViewerActivity::class.java).apply {
            putExtra("TRACK_ID", track.trackId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

}
