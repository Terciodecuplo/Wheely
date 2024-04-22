package com.jmblfma.wheely.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jmblfma.wheely.R
import com.jmblfma.wheely.model.Vehicle

class ProfileVehicleListAdapter(
    val vehicleList: ArrayList<Vehicle>,
    var context: Context
) : RecyclerView.Adapter<ProfileVehicleListAdapter.MyHolder>() {

    class MyHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView
        val motoModel: TextView

        init {
            imageView = view.findViewById(R.id.vehicle_preview_image)
            motoModel = view.findViewById(R.id.motoModel)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vehicle_list_recycler, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val vehicle = vehicleList[position]
        Log.d("VehicleAdapter","Binding vehicle: ${vehicle.model}")
        holder.imageView.setImageResource(R.drawable.pic_vehicle_template)
        holder.motoModel.text = vehicle.model
    }

    override fun getItemCount() = vehicleList.size


    fun updateVehicles(newVehicles: MutableList<Vehicle>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = vehicleList.size

            override fun getNewListSize(): Int = newVehicles.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return vehicleList[oldItemPosition].vehicleId == newVehicles[newItemPosition].vehicleId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return vehicleList[oldItemPosition] == newVehicles[newItemPosition]
            }
        })

        vehicleList.clear()
        vehicleList.addAll(newVehicles)
        diffResult.dispatchUpdatesTo(this)
    }

}