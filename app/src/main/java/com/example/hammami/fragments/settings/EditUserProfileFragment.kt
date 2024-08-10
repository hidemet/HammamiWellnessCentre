package com.example.hammami.fragments.settings

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.hammami.R
import com.example.hammami.databinding.FragmentEditUserProfileBinding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.models.User
import com.example.hammami.viewmodel.EditUserProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.Manifest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.hammami.databinding.BottomSheetResetPasswordBinding
import com.example.hammami.util.StringValidators
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar


@AndroidEntryPoint
class EditUserProfileFragment : BaseFragment() {
    private var _binding: FragmentEditUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditUserProfileViewModel by activityViewModels()
    private lateinit var bottomSheetDialog: BottomSheetDialog


    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var takePicture: ActivityResultLauncher<Uri>

    private val REQUEST_CODE_PERMISSIONS = 1001
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { handleImageResult(it) }
        }

        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                imageUri?.let { handleImageResult(it) }
            }
        }
    }
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
                openResetPasswordDialog()
            }

            editProfileImageButton.setOnClickListener {
                showImageSourceDialog()
            }
        }
    }

    private fun showImageSourceDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.choose_image_source))
            .setItems(R.array.image_sources) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            .show()
    }



    private fun openGallery() {
        getContent.launch("image/*")
    }

    private fun openCamera() {
        val uri = viewModel.createImageUri(requireContext())
        imageUri = uri
        takePicture.launch(uri)
    }

    private fun handleImageResult(uri: Uri) {
        viewModel.uploadProfileImage(uri)
    }

//    private fun updateProfileImage(imageUrl: String) {
//        Glide.with(this)
//            .load(imageUrl)
//            .apply(RequestOptions()
//                .placeholder(R.drawable.default_profile_image)
//                .error(R.drawable.default_profile_image)
//                .diskCacheStrategy(DiskCacheStrategy.ALL))
//            .into(binding.profileImageView)
//
//        viewModel.updateUserProfileImage(imageUrl, requireContext())
//    }


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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collectResource(
                onSuccess = { user ->
                    updateUIWithUserData(user)
                },
                onError = { errorMessage ->
                    showSnackbar(errorMessage ?: getString(R.string.unknown_error))
                }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.profileUpdateEvent.collect { result ->
                when (result) {
                    is EditUserProfileViewModel.ProfileUpdateResult.Success -> {
                        showSnackbar(result.message)
                        viewModel.fetchUserProfile()
                    }
                    is EditUserProfileViewModel.ProfileUpdateResult.Error -> {
                        showSnackbar(result.message)
                    }
                }
            }
        }
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

private fun openResetPasswordDialog() {
    val bottomSheetBinding = BottomSheetResetPasswordBinding.inflate(layoutInflater)
    bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
        setContentView(bottomSheetBinding.root)

        bottomSheetBinding.buttonCancel.setOnClickListener { dismiss() }
        bottomSheetBinding.buttonConfirm.setOnClickListener {
            if (ValidationUtil.validateField(bottomSheetBinding.textFieldEmail, StringValidators.Email)) {
                val email = bottomSheetBinding.textFieldEmail.editText?.text.toString()
                viewModel.resetPassword(email)
                observePasswordChangeEvent()
                dismiss()
            }
        }
    }
    bottomSheetDialog.show()
}

private fun observePasswordChangeEvent() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.passwordChangeEvent.collect { result ->
                when (result) {
                    is EditUserProfileViewModel.PasswordChangeResult.Success -> {
                        Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                    }
                    is EditUserProfileViewModel.PasswordChangeResult.Error -> {
                        Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}