package com.jmblfma.wheely.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmblfma.wheely.R
import com.jmblfma.wheely.adapter.ProfileVehicleListAdapter
import com.jmblfma.wheely.viewmodels.NewVehicleDataViewModel

class VehicleFragment : Fragment() {

    private lateinit var profileVehicleListAdapter: ProfileVehicleListAdapter
    private lateinit var gridRecyclerView: RecyclerView
    private lateinit var viewModel: NewVehicleDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(NewVehicleDataViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_vehicle, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gridRecyclerView = view.findViewById(R.id.vehicle_grid_recycler)
        profileVehicleListAdapter = ProfileVehicleListAdapter(ArrayList(), requireContext())

        gridRecyclerView.layoutManager = GridLayoutManager(context, 3)
        gridRecyclerView.adapter = profileVehicleListAdapter
/*
        viewModel.vehicles.observe(viewLifecycleOwner) { vehicles ->
            Log.d("VehicleFragment", "Received vehicle update: ${vehicles.size}")
            profileVehicleListAdapter.updateVehicles(ArrayList(vehicles))

        }*/
    }

    companion object {
        fun newInstance(): VehicleFragment {
            return VehicleFragment()
        }
    }
}
