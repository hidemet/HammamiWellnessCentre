package com.example.hammami.presentation.ui.features.client

import android.os.Bundle
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.hammami.R
import com.example.hammami.databinding.FragmentHomeBinding
import com.example.hammami.presentation.ui.adapters.HomeViewpagerAdapter
import com.example.hammami.presentation.ui.features.categories.MainCategoryFragment
import com.example.hammami.presentation.viewmodel.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
   // private val viewModel: UserProfileViewModel by activityViewModels()
   private val viewModel: HomeViewModel by viewModels()


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

        setupViewPager()
        setupTopAppBar()
    }

    private fun setupViewPager() {
        val categoriesFragments = arrayListOf<Fragment>(
            MainCategoryFragment()
        )

        val viewPager2Adapter = HomeViewpagerAdapter(categoriesFragments, childFragmentManager, lifecycle)
        binding.viewpagerHome.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewpagerHome.adapter = viewPager2Adapter
    }

    private fun setupTopAppBar() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.profile -> {
                    handleProfileNavigation()
                    true
                }
                R.id.notify -> {
                    // Handle notifications
                    true
                }
                else -> false
            }
        }
    }

    private fun handleProfileNavigation() {
        if (viewModel.isUserAuthenticated()) {
            findNavController().navigate(R.id.action_global_profileFragment)
        } else {
            Snackbar.make(
                binding.root,
                getString(R.string.error_auth_required),
                Snackbar.LENGTH_LONG
            ).show()
            findNavController().navigate(R.id.loginFragment)
        }
    }


}