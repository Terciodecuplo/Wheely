package com.jmblfma.wheely.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jmblfma.wheely.fragment.TrackHistoryFragment
import com.jmblfma.wheely.fragment.VehicleFragment

class ProfileViewPagerAdapter(
    fragmentActivity: FragmentActivity,
) : FragmentStateAdapter(fragmentActivity) {

    private val TAB_TITLES = arrayOf("History", "Vehicles")

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TrackHistoryFragment.newInstance()
            1 -> VehicleFragment.newInstance()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }

    override fun getItemCount(): Int = TAB_TITLES.size

}
