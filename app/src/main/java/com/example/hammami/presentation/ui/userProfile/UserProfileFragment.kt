package com.example.hammami.presentation.ui.userProfile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileFragment : BaseFragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            viewModel.state.collectLatest { state ->
                state.user?.let { updateUIWithUserData(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvent.collectLatest { event ->
                handleUiEvent(event)
            }
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

    private fun updateUIWithUserData(user: User) {
        binding.apply {
            userName.text = "${user.firstName} ${user.lastName}"
            userPoints.text = getString(R.string.user_points, user.toString())

            Glide.with(this@UserProfileFragment)
                .load(user.profileImage)
                .error(R.drawable.ic_profile)
                .into(profileImage)
        }


    }

    private fun handleUiEvent(event: UserProfileViewModel.UiEvent) {
        when (event) {
            is UserProfileViewModel.UiEvent.ShowError -> showSnackbar(event.error)
            is UserProfileViewModel.UiEvent.ProfileUpdateSuccess -> showSnackbar(event.message)
            is UserProfileViewModel.UiEvent.SignOutSuccess -> navigateToLoginFragment()
            is UserProfileViewModel.UiEvent.UserDeleted -> navigateToLoginFragment()
            is UserProfileViewModel.UiEvent.Loading -> showLoading(true)
            is UserProfileViewModel.UiEvent.Idle -> showLoading(false)
            else -> {}
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.linearProgressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentScrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }


    private fun navigateToLoginFragment() {
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