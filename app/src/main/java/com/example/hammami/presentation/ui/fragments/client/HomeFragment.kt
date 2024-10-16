package com.example.hammami.presentation.ui.fragments.client

import android.os.Bundle
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.hammami.R
import com.example.hammami.databinding.FragmentHomeBinding
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import com.example.hammami.presentation.ui.adapters.HomeViewpagerAdapter
import com.example.hammami.presentation.ui.fragments.categories.MainCategoryFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: UserProfileViewModel by activityViewModels()

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
        observeUserState()
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
                R.id.notify -> {
                    // Handle settings icon press
                    true
                }
                R.id.profile -> {
                    findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun observeUserState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userState.collect { state ->
                when (state) {
                    is UserProfileViewModel.UserState.LoggedIn -> {
                        // Aggiorna l'UI mostrando l'avatar dell'utente
                    }
                    is UserProfileViewModel.UserState.Error -> {
                        // Gestisci eventuali errori
                    }
                    else -> { /* Handle other states */}
                }
            }
        }
    }
}