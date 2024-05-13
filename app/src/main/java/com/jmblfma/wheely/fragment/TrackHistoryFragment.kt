package com.jmblfma.wheely.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmblfma.wheely.R
import com.jmblfma.wheely.adapter.TrackHistoryAdapter
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.viewmodels.UserDataViewModel

class TrackHistoryFragment : Fragment() {

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
            TrackHistoryAdapter(trackHistoryList, userVehicles, requireContext())

        view.findViewById<RecyclerView>(R.id.trackHistory_recycler).apply {
            adapter = trackHistoryAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
            scrollToPosition(trackHistoryList.size - 1)
        }
    }
}
