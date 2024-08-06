package com.example.hammami.fragments.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.hammami.R
import com.example.hammami.databinding.FragmentEditUserProfileBinding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.models.User
import com.example.hammami.viewmodel.EditUserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditUserProfileFragment : BaseFragment() {
    private var _binding: FragmentEditUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditUserProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun setupUI() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackClick()
            }

            editPersonalInfoButton.setOnClickListener {
                openEditPersonalInfoDialog()
            }

            editContactsButton.setOnClickListener {
                openEditContactsDialog()
            }

            changePasswordButton.setOnClickListener {
                openChangePasswordDialog()
            }

            editProfileImageButton.setOnClickListener {
                onEditProfileImageClick()
                showSnackbar("Change profile image functionality not implemented yet")
            }
        }
    }

    private fun onEditProfileImageClick() {
            // Implement this method
    }


//    override fun observeFlows() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.userProfileState.collect { resource ->
//                Log.d("EditUserProfile", "Received user profile state: $resource")
//                when (resource) {
//                    is Resource.Success -> {
//                        showLoading(false)
//                        resource.data?.let { user ->
//                            Log.d("EditUserProfile", "Updating UI with user data: $user")
//                            updateUIWithUserData(user)
//                        }
//                    }
//                    is Resource.Loading -> showLoading(true)
//                    is Resource.Error -> {
//                        showLoading(false)
//                        showSnackbar(resource.message ?: getString(R.string.unknown_error))
//                    }
//                    is Resource.Unspecified -> {}
//                }
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.profileUpdateEvent.collect { result ->
//                Log.d("EditUserProfile", "Received profile update event: $result")
//                when (result) {
//                    is EditUserProfileViewModel.ProfileUpdateResult.Success -> {
//                        showSnackbar(result.message)
//                        viewModel.fetchUserProfile()
//                    }
//                    is EditUserProfileViewModel.ProfileUpdateResult.Error -> {
//                        showSnackbar(result.message)
//                    }
//                }
//            }
//        }
//    }

//    override fun observeFlows() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.currentUser.collect { user ->
//                Log.d("EditUserProfile", "Received updated user: $user")
//                user?.let { updateUIWithUserData(it) }
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.profileUpdateEvent.collect { result ->
//                Log.d("EditUserProfile", "Received profile update event: $result")
//                when (result) {
//                    is EditUserProfileViewModel.ProfileUpdateResult.Success -> {
//                        showSnackbar(result.message)
//                        viewModel.fetchUserProfile()
//                    }
//                    is EditUserProfileViewModel.ProfileUpdateResult.Error -> {
//                        showSnackbar(result.message)
//                    }
//                }
//            }
//        }
//    }


    override fun observeFlows() {
        viewModel.user.collectResource(
            onSuccess = { user ->
                updateUIWithUserData(user)
            },
            onError = { errorMessage ->
                showSnackbar(errorMessage ?: getString(R.string.unknown_error))
            }
        )
    }
    override fun onResume() {
        super.onResume()
        viewModel.fetchUserProfile()
    }


    override fun showLoading(isLoading: Boolean) {
        binding.linearProgressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentScrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun updateUIWithUserData(user: User) {

        if (!isAdded || view == null) {
            Log.w("EditUserProfile", "Fragment not added or view is null. Skipping UI update.")
            return
        }

        binding.apply {
            firstName.text = user.firstName
            lastName.text = user.lastName
            birthDate.text = user.birthDate
            allergies.text = user.allergies.ifEmpty { "-" }
            gender.text = user.gender
            disabilities.text = user.disabilities.ifEmpty { "-" }
            phoneNumberValue.text = user.phoneNumber
            emailAddressValue.text = user.email

            if (user.profileImage.isNotEmpty()) {
                Glide.with(this@EditUserProfileFragment)
                    .load(user.profileImage)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.default_profile_image)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.default_profile_image)
            }
        }
        Log.d("EditUserProfile", "UI updated with user data: $user")

    }

    private fun openEditPersonalInfoDialog() {
        try {
            EditPersonalInfoDialogFragment().show(parentFragmentManager, "EditPersonalInfoDialog")

        } catch (e: Exception) {
            showSnackbar(getString(R.string.error_showing_edit_profile))
        }
    }

    private fun openEditContactsDialog() {
        try {
            EditContactInfoDialogFragment().show(parentFragmentManager, "EditContactInfoDialog")
        } catch (e: Exception) {
            showSnackbar(getString(R.string.error_showing_edit_profile))
        }
    }

    private fun openChangePasswordDialog() {
        // Implement this method
        showSnackbar("Change password functionality not implemented yet")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}