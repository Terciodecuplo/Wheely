package com.jmblfma.wheely.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmblfma.wheely.R
import com.jmblfma.wheely.VehicleStatsActivity
import com.jmblfma.wheely.adapter.ProfileVehicleListAdapter
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.repository.VehicleDataRepository
import com.jmblfma.wheely.utils.UserSessionManager
import com.jmblfma.wheely.viewmodels.NewVehicleDataViewModel
import kotlinx.coroutines.launch

class VehicleFragment : Fragment(), ProfileVehicleListAdapter.OnVehicleItemClickListener{

    private lateinit var profileVehicleListAdapter: ProfileVehicleListAdapter
    private lateinit var gridRecyclerView: RecyclerView
    private lateinit var vehicleList: List<Vehicle>
    private val repository = VehicleDataRepository.sharedInstance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchVehicles()
    }

    override fun onResume() {
        super.onResume()
        fetchVehicles()
    }

    private fun fetchVehicles() {
        lifecycleScope.launch {
            val userId = UserSessionManager.getCurrentUser()?.userId
            if (userId != null) {
                vehicleList = repository.fetchVehicles(userId)
                profileVehicleListAdapter.updateVehicles(vehicleList)
            }
        }
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
        profileVehicleListAdapter = ProfileVehicleListAdapter(ArrayList(), requireContext(), this)

        gridRecyclerView.layoutManager = GridLayoutManager(context, 3)
        gridRecyclerView.adapter = profileVehicleListAdapter
    }
    override fun onVehicleItemClick(vehicle: Vehicle) {
        // Handle the click event here
        // You have the clicked vehicle, do whatever you want with it
        // For example, you can navigate to another activity to display vehicle details
        val intent = Intent(requireContext(), VehicleStatsActivity::class.java)
        intent.putExtra("vehicle_id", vehicle.vehicleId)
        startActivity(intent)
    }

    companion object {
        fun newInstance(): VehicleFragment {
            return VehicleFragment()
        }
    }
}
