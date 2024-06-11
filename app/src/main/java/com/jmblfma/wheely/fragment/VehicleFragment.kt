package com.jmblfma.wheely.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import com.jmblfma.wheely.viewmodels.UserDataViewModel
import kotlinx.coroutines.launch

class VehicleFragment : Fragment(), ProfileVehicleListAdapter.OnVehicleItemClickListener{

    private lateinit var profileVehicleListAdapter: ProfileVehicleListAdapter
    private lateinit var gridRecyclerView: RecyclerView
    private val viewModel: NewVehicleDataViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        val userId = UserSessionManager.getCurrentUser()!!.userId
        viewModel.fetchVehicleList(userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.vehicleList.observe(viewLifecycleOwner){ userVehicles ->
            Log.d("TESTING", "There is moto")
            profileVehicleListAdapter.updateVehicles(userVehicles)
        }
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
        val intent = Intent(requireContext(), VehicleStatsActivity::class.java)
        intent.putExtra("vehicleId", vehicle.vehicleId)
        // Log.d("VEHICLE", "Vehicle selected = $vehicle")
        startActivity(intent)
    }
}
