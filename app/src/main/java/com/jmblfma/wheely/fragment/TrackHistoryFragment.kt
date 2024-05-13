package com.jmblfma.wheely.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmblfma.wheely.R
import com.jmblfma.wheely.adapter.ProfileVehicleListAdapter
import com.jmblfma.wheely.adapter.TrackHistoryAdapter
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.UserDataViewModel

class TrackHistoryFragment : Fragment() {

    private var trackHistoryList: List<Track> = emptyList()
    private lateinit var trackHistoryAdapter: TrackHistoryAdapter
    private val viewModel: UserDataViewModel by viewModels()

    private fun fetchTrackList() {
        viewModel.trackListLoader.observe(viewLifecycleOwner){
            Log.d("TESTING","First item on the list is ${it.get(0).name}")
            trackHistoryList = it
        }
    }

    override fun onResume() {
        super.onResume()
        fetchTrackList()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_track_history, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TESTING","View created")
        val userId = UserSessionManager.getCurrentUser()!!.userId
        viewModel.fetchUserTrackList(userId)
        fetchTrackList()
        setupRecyclerView(view)
    }

    private fun setupRecyclerView(view: View) {
        trackHistoryAdapter =
            TrackHistoryAdapter(trackHistoryList, requireContext())

        view.findViewById<RecyclerView>(R.id.trackHistory_recycler).apply {
            adapter = trackHistoryAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    companion object {
        fun newInstance(): TrackHistoryFragment {
            return TrackHistoryFragment()
        }
    }
}
