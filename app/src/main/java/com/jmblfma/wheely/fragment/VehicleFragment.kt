package com.jmblfma.wheely.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmblfma.wheely.R
import com.jmblfma.wheely.adapter.ProfileVehicleListAdapter
import com.jmblfma.wheely.model.Vehicle

class VehicleFragment() : Fragment() {

    private var vehicleList: ArrayList<Vehicle>? = null
    private lateinit var profileVehicleListAdapter: ProfileVehicleListAdapter
    private lateinit var gridRecyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vehicleList = it.getParcelableArrayList<Vehicle>(ARG_VEHICLE_LIST) ?: arrayListOf()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_vehicle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gridRecyclerView = view.findViewById(R.id.vehicle_grid_recycler)
        profileVehicleListAdapter = ProfileVehicleListAdapter(vehicleList?: arrayListOf(), requireContext())

        gridRecyclerView.layoutManager = GridLayoutManager(context, 3)
        gridRecyclerView.adapter = profileVehicleListAdapter
    }

    companion object {
        private const val ARG_VEHICLE_LIST = "vehicle_list"

        fun newInstance(vehicleList: ArrayList<Vehicle>): VehicleFragment {
            val fragment = VehicleFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_VEHICLE_LIST, vehicleList)
            fragment.arguments = args
            return fragment
        }
    }
}
