package com.example.hammami.presentation.ui.fragments.client

import android.os.Bundle
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import com.example.hammami.R
import com.example.hammami.databinding.FragmentHomeBinding
import com.example.hammami.presentation.ui.adapters.HomeViewpagerAdapter
import com.example.hammami.presentation.ui.fragments.categories.MainCategoryFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesFragments = arrayListOf<Fragment>(
            MainCategoryFragment()
        )

        val viewPager2Adapter =
            HomeViewpagerAdapter(categoriesFragments, childFragmentManager, lifecycle)
        binding.viewpagerHome.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewpagerHome.adapter = viewPager2Adapter

        //binding = FragmentHomeBinding.bind(view)

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.notify -> {
                    // Handle settings icon press
                    true
                }

                R.id.profile -> {
                    Navigation.findNavController(view)
                        .navigate(R.id.action_homeFragment_to_profileFragment)
                    true
                }

                else -> false
            }
        }

    }


}