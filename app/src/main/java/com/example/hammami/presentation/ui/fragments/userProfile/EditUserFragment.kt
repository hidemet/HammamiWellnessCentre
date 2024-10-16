package com.example.hammami.presentation.ui.fragments.userProfile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.FragmentEditUserProfileBinding
import com.example.hammami.model.User
import com.example.hammami.presentation.ui.activities.LoginRegisterActivity
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import com.example.hammami.presentation.ui.fragments.BaseFragment
import com.example.hammami.presentation.ui.fragments.userProfile.editUser.ResetPasswordDialogFragment
import com.example.hammami.presentation.ui.fragments.userProfile.editUser.EditContactInfoDialogFragment
import com.example.hammami.presentation.ui.fragments.userProfile.editUser.EditPersonalInfoDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class EditUserFragment : BaseFragment() {
    private var _binding: FragmentEditUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by activityViewModels()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { viewModel.uploadProfileImage(it) }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoading(true)
        setupUI()
        observeFlows()
    }
    override fun setupUI() {
        binding.apply {
            topAppBar.setNavigationOnClickListener { onBackClick() }
            editProfileImageButton.setOnClickListener { pickImageLauncher.launch("image/*") }
            editPersonalInfoButton.setOnClickListener { showEditPersonalInfoDialog() }
            editContactsInfoButton.setOnClickListener { showEditContactDialog() }
            changePasswordButton.setOnClickListener { showResetPasswordDialog() }
            deleteAccountButton.setOnClickListener { showDeleteAccountConfirmationDialog() }
        }
    }

    private fun showEditPersonalInfoDialog() {
        val dialog = EditPersonalInfoDialogFragment.newInstance()
        dialog.show(childFragmentManager, "EditPersonalInfoDialog")
    }

    private fun showEditContactDialog() {
        EditContactInfoDialogFragment.newInstance().show(childFragmentManager, "EditContactInfoDialog")
    }

    private fun showResetPasswordDialog() {
        ResetPasswordDialogFragment.newInstance().show(parentFragmentManager, "ResetPasswordDialog")
    }


    private fun showDeleteAccountConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.delete_account_warning_title)
            .setMessage(R.string.delete_account_warning_message)
            .setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.confirm) { _, _ ->
                viewModel.deleteAccount()
            }.show()
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.userState.collectLatest { state ->
                    when (state) {
                        is UserProfileViewModel.UserState.LoggedIn -> updateUI(state.userData)
                        is UserProfileViewModel.UserState.Error -> showError(state.message)
                        is UserProfileViewModel.UserState.Loading -> showLoading(true)
                        is UserProfileViewModel.UserState.NotLoggedIn -> navigateToLoginActivity()
                    }
                }
            }
            launch {
                viewModel.uiState.collectLatest { state ->
                    handleUiState(state)
                }
            }
        }
    }

    private fun updateUI(user: User) {
        binding.apply {
            firstName.text = user.firstName
            lastName.text = user.lastName
            emailAddressValue.text = user.email
            phoneNumberValue.text = user.phoneNumber
            birthDate.text = user.birthDate
            gender.text = user.gender
            allergies.text = user.allergies
            disabilities.text = user.disabilities

            Glide.with(this@EditUserFragment).load(user.profileImage)
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image).into(profileImageView)
        }
        showLoading(false)
    }

    private fun handleUiState(state: UserProfileViewModel.UiState) {
        when (state) {
            is UserProfileViewModel.UiState.Success -> {
                showSnackbar(state.message)
                viewModel.loadUserData()
            }

            is UserProfileViewModel.UiState.Error -> showError(state.message)
            is UserProfileViewModel.UiState.Loading -> showLoading(true)
            is UserProfileViewModel.UiState.Idle -> showLoading(false)
            is UserProfileViewModel.UiState.AccountDeleted -> navigateToLoginActivity()
        }
    }


    private fun navigateToLoginActivity() {
        val intent = Intent(requireContext(), LoginRegisterActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showError(message: UiText) {
        showSnackbar(message)
        showLoading(false)
    }


    override fun showLoading(isLoading: Boolean) {
        binding.linearProgressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentScrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}