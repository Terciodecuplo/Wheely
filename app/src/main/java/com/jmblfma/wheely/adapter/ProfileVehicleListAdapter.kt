package com.jmblfma.wheely.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
        val vehicles = vehicleList[position]
        holder.imageView.setImageResource(R.drawable.pic_vehicle_template)
        holder.motoModel.text = vehicles.model
    }

    override fun getItemCount() = vehicleList.size
}