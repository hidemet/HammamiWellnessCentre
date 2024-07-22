package com.example.hammami.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.hammami.R
import com.example.hammami.databinding.FragmentEditProfileBinding
import com.example.hammami.models.User
import com.example.hammami.util.RegisterFieldsState
import com.example.hammami.util.RegisterValidation
import com.example.hammami.util.Resource
import com.example.hammami.viewmodel.EditProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : DialogFragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.topAppBar.setNavigationOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            saveUserData()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userState.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        showLoading(false)
                        populateUserData(resource.data)
                    }
                    is Resource.Loading -> showLoading(true)
                    is Resource.Error -> {
                        showLoading(false)
                        showError(resource.message ?: getString(R.string.unknown_error))
                    }
                    is Resource.Unspecified -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.validationState.collectLatest { state ->
                state?.let { showValidationErrors(it) }
            }
        }
    }

    private fun populateUserData(user: User) {
        with(binding) {
            firstNameEditText.setText(user.firstName)
            lastNameEditText.setText(user.lastName)
            // Populate other fields...
        }
    }

    private fun saveUserData() {
        val updatedUser = User(
            firstName = binding.firstNameEditText.text.toString(),
            lastName = binding.lastNameEditText.text.toString(),
            // Get other fields...
        )
        viewModel.validateAndUpdateUser(updatedUser)
    }

    private fun showValidationErrors(state: RegisterFieldsState) {
        with(binding) {
            firstNameInputLayout.error = (state.firstName as? RegisterValidation.Failed)?.message
            lastNameInputLayout.error = (state.lastName as? RegisterValidation.Failed)?.message
            // Show other validation errors...
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.contentLayout.isVisible = !isLoading
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}