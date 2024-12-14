package com.example.hammami.presentation.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hammami.presentation.ui.features.appointments.NewAppointmentsFragment
import com.example.hammami.presentation.ui.features.appointments.PastAppointmentsFragment

class AppointmentsViewPagerAdapter (fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    //positioning fragments in tablayout list
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                NewAppointmentsFragment()
            }
            1 -> {
                PastAppointmentsFragment()
            }
            else -> {
                NewAppointmentsFragment()
            }
        }
    }
}