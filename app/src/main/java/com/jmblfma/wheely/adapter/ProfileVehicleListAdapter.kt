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
import com.bumptech.glide.Glide
import com.jmblfma.wheely.R
import com.jmblfma.wheely.model.Vehicle

class ProfileVehicleListAdapter(
    val vehicleList: ArrayList<Vehicle>,
    var context: Context,
    private val itemClickListener: OnVehicleItemClickListener
) : RecyclerView.Adapter<ProfileVehicleListAdapter.MyHolder>() {
    interface OnVehicleItemClickListener {
        fun onVehicleItemClick(vehicle: Vehicle)
    }

    class MyHolder(
        view: View,
        private val vehicleList: List<Vehicle>,
        private val itemClickListener: OnVehicleItemClickListener
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val imageView: ImageView
        val motoModel: TextView

        init {
            imageView = view.findViewById(R.id.vehicle_preview_image)
            motoModel = view.findViewById(R.id.motoModel)
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            // Get the clicked vehicle
            val clickedVehicle = vehicleList[adapterPosition]
            // Invoke the interface method to notify the click event
            itemClickListener.onVehicleItemClick(clickedVehicle)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vehicle_list_recycler, parent, false)
        return MyHolder(view, vehicleList, itemClickListener)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val vehicle = vehicleList[position]
        Log.d("VehicleAdapter", "Binding vehicle: ${vehicle.model}")
        setVehicleImage(holder.imageView, vehicle.image)
        holder.motoModel.text = vehicle.model

    }

    override fun getItemCount() = vehicleList.size


    fun updateVehicles(newVehicles: List<Vehicle>) {
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

    fun setVehicleImage(imageView: ImageView, imagePath: String?) {
        if (!imagePath!!.startsWith("/")) {
            Glide.with(imageView.context)
                .load(R.drawable.pic_vehicle_template) // Your placeholder drawable
                .into(imageView)
        } else {
            Glide.with(imageView.context)
                .load(imagePath)
                .into(imageView)
        }
    }

}