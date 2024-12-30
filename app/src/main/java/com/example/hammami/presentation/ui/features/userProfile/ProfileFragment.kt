package com.example.hammami.presentation.ui.features.userProfile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.hammami.R
import com.example.hammami.databinding.FragmentUserProfileBinding
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.domain.model.ItemProfileOption
import com.example.hammami.domain.model.User
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import com.example.hammami.presentation.ui.activities.UserProfileViewModel.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        with(binding) {
            topAppBar.setNavigationOnClickListener { onBackClick() }
            editProfileButton.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_editUserProfileFragment)
            }
            logoutButton.setOnClickListener {
                Log.d("ProfileFragment", "Logout button clicked") // Log di debug
                viewModel.signOut()
            }
        }
    }

//    override fun onBackClick() {
//        requireActivity().finish()
//    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeUiState() }
                launch { observeUiEvents() }
            }
        }
    }

    private suspend fun observeUiState() {
        viewModel.uiState.collect { state ->
            showLoading(state.isLoading)
            state.user?.let { updateUI(it) }
        }
    }

    private suspend fun observeUiEvents() {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UserProfileViewModel.UiEvent.UserMessage -> showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    private fun updateUI(user: User) = with(binding) {
        userName.text = "${user.firstName} ${user.lastName}"
        userPoints.text = getString(R.string.user_points, user.points)

        Glide.with(this@ProfileFragment)
            .load(user.profileImage)
            .error(R.drawable.ic_profile)
            .into(profileImage)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeFlows()
        setupOptionsList()
    }

    override fun onStart() {
        super.onStart()
        // Ora è sicuro chiamare findNavController()
        setupOptionsList()
    }

    private fun setupOptionsList() {
        val options = listOf(
            ItemProfileOption(
                "Gift Card",
                R.drawable.ic_gift_card,
                action = {
                    findNavController().navigate(R.id.giftCardsFragment)
                }
            ),
            ItemProfileOption(
                "Coupon",
                R.drawable.ic_coupon,
                action = {
                    findNavController().navigate(R.id.action_profileFragment_to_couponFragment)
                }
            )
        )

        binding.optionsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ProfileOptionAdapter(options)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.linearProgressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}