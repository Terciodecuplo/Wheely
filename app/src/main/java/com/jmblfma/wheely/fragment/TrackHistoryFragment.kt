package com.jmblfma.wheely.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmblfma.wheely.R
import com.jmblfma.wheely.adapter.TrackHistoryAdapter
import com.jmblfma.wheely.model.Track

class TrackHistoryFragment : Fragment() {

    private var trackHistoryList: ArrayList<Track>? = null
    private lateinit var trackHistoryAdapter: TrackHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            trackHistoryList = it.getParcelableArrayList<Track>(ARG_TRACK_HISTORY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_track_history, container, false)
        setupRecyclerView(view)
        return view
    }

    private fun setupRecyclerView(view: View) {
        // Initialize the adapter with the track history list
        trackHistoryAdapter = TrackHistoryAdapter(trackHistoryList ?: arrayListOf(), requireContext())

        // Setup RecyclerView
        view.findViewById<RecyclerView>(R.id.trackHistory_recycler).apply {
            adapter = trackHistoryAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    companion object {
        private const val ARG_TRACK_HISTORY = "track_history"

        // Method to create a new instance of HistoryFragment with track history data
        fun newInstance(trackHistoryList: ArrayList<Track>): TrackHistoryFragment {
            val fragment = TrackHistoryFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_TRACK_HISTORY, trackHistoryList)
            fragment.arguments = args
            return fragment
        }
    }
}
