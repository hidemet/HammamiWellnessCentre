package com.example.hammami.presentation.ui.features.client

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.hammami.R
import com.example.hammami.presentation.ui.adapters.AppointmentsViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AppointmentsFragment: Fragment(R.layout.fragment_appointments) {

    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: AppointmentsViewPagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tlAppuntamenti) ?: return
        viewPager2 = view.findViewById(R.id.vpAppuntamenti) ?: ViewPager2(requireContext())
        adapter = AppointmentsViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "In programma"
                }
                1 -> {
                    tab.text = "Passati"
                }
            }
        }.attach()
    }
}