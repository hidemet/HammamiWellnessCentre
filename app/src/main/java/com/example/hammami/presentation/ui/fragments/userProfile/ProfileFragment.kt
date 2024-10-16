package com.example.hammami.presentation.ui.fragments.userProfile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.hammami.R
import com.example.hammami.databinding.FragmentUserProfileBinding
import com.example.hammami.presentation.ui.fragments.BaseFragment
import com.example.hammami.model.ItemProfileOption
import com.example.hammami.model.User
import com.example.hammami.presentation.ui.activities.LoginRegisterActivity
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import com.example.hammami.presentation.ui.userProfile.ProfileOptionAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun setupUI() {
        setupOptionsList()
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editUserProfileFragment)
        }
        binding.logoutButton.setOnClickListener {
            viewModel.signOut()
        }
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userState.collect { state ->
                when (state) {
                    is UserProfileViewModel.UserState.LoggedIn -> updateUI(state.userData)
                    is UserProfileViewModel.UserState.Error -> showSnackbar(state.message)
                    is UserProfileViewModel.UserState.NotLoggedIn -> navigateToLogin()
                    is UserProfileViewModel.UserState.Loading -> showLoading(true)
                }
            }
        }
    }

    private fun updateUI(userData: User) {
        binding.apply {
            userName.text = "${userData.firstName} ${userData.lastName}"
            userPoints.text = getString(R.string.user_points, userData.points)

            Glide.with(this@ProfileFragment)
                .load(userData.profileImage)
                .error(R.drawable.ic_profile)
                .into(profileImage)
        }
    }


    private fun setupOptionsList() {
        val options = listOf(
            ItemProfileOption(
                "Gift Card",
                R.drawable.ic_gift_card,
                navigationDestination = R.id.giftCardsFragment
            ),
            ItemProfileOption(
                "Coupon",
                R.drawable.ic_coupon,
                navigationDestination = R.id.couponFragment
            )
        )

        binding.optionsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ProfileOptionAdapter(options)
        }
    }


    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginRegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}