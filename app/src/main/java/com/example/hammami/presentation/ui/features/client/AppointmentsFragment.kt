package com.example.hammami.presentation.ui.features.client

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.hammami.R
import com.example.hammami.presentation.ui.adapters.AppointmentsViewPagerAdapter
import com.example.hammami.presentation.ui.features.appointments.AddReviewFragment
import com.example.hammami.presentation.ui.features.appointments.AppointmentsViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppointmentsFragment: Fragment(R.layout.fragment_appointments) {

    private lateinit var viewPager2: ViewPager2
    private val viewModel: AppointmentsViewModel by activityViewModels()
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

        observeFlows()
    }

    fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeReviewAdded() }
            }
        }
    }

    private suspend fun observeReviewAdded() {
        viewModel.newlyAddedReview.collect { reviewData ->
            reviewData?.let { (serviceName, review) ->
                if (findNavController().currentDestination?.id == R.id.appointmentsFragment) {
                    if (findNavController().currentDestination?.id == R.id.appointmentsFragment) {
                        val action =
                            AppointmentsFragmentDirections.actionAppointmentsFragmentToReviewSummaryFragment(
                                serviceName,
                                review
                            )
                        findNavController().navigate(action)
                    }
                    viewModel.resetReviewAdded()

                }
            }
        }
    }
}
