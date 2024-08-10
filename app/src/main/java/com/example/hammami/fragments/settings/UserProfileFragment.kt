package com.example.hammami.fragments.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.hammami.R
import com.example.hammami.adapters.ProfileOptionAdapter
import com.example.hammami.databinding.FragmentUserProfileBinding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.fragments.loginResigter.RegisterFragment1Directions
import com.example.hammami.models.ItemProfileOption
import com.example.hammami.models.User
import com.example.hammami.viewmodel.UserProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

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
        binding.topAppBar.setNavigationOnClickListener {
            onBackClick()
        }
        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(UserProfileFragmentDirections.actionProfileFragmentToEditUserProfileFragment())
        }
    }

    override fun observeFlows() {
        viewModel.user.collectResource(
            onSuccess = { user ->
                updateUIWithUserData(user)
            },
            onError = { errorMessage ->
                showErrorWithRetry(errorMessage ?: getString(R.string.unknown_error))
            }
        )
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
            userPoints.text = "${user.points} punti"

            Glide.with(this@UserProfileFragment)
                .load(user.profileImage)
                .error(R.drawable.ic_profile)
                .into(profileImage)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.linearProgressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentScrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showErrorWithRetry(message: String) {
        showSnackbar(message, actionText = getString(R.string.retry)) {
            viewModel.refreshUser()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}