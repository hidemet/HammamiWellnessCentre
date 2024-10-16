package com.example.hammami.presentation.ui.fragments.userProfile.editUser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.hammami.databinding.DialogEditContactInfoBinding
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import com.example.hammami.util.hideKeyboard
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditContactInfoDialogFragment : BottomSheetDialogFragment() {
    private var _binding: DialogEditContactInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogEditContactInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        with(binding) {
            topAppBar.setNavigationOnClickListener { dismiss() }
            saveButton.setOnClickListener { onSaveButtonClick() }

            viewModel.userState.value.let { state ->
                if (state is UserProfileViewModel.UserState.LoggedIn) {
                    val user = state.userData
                    phoneNumberEditText.setText(user.phoneNumber)
                    emailEditText.setText(user.email)
                }
            }

            listOf(phoneNumberEditText, emailEditText).forEach {
                it.addTextChangedListener { onTextChanged() }
            }
        }
    }

    private fun onTextChanged() {
        binding.saveButton.isVisible = true

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.validationState.collect { state ->
                    updateUIWithErrors(state)
                }
            }
            launch {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }

    private fun updateUIWithErrors(state: UserProfileViewModel.ValidationState) {
        binding.apply {
            phoneNumberInputLayout.error = state.phoneNumberError?.asString(requireContext())
            emailInputLayout.error = state.emailError?.asString(requireContext())
        }
    }

    private fun handleUiState(state: UserProfileViewModel.UiState) {
        when (state) {
            is UserProfileViewModel.UiState.Success -> {
                showSuccessMessage(state.message.asString(requireContext()))
                dismiss()
            }
            is UserProfileViewModel.UiState.Error -> showError(state.message.asString(requireContext()))
            is UserProfileViewModel.UiState.Loading -> showLoading(true)
            else -> showLoading(false)
        }
    }

    private fun onSaveButtonClick() {
        hideKeyboard()
        val contactInfo = UserProfileViewModel.ContactInfo(
            phoneNumber = binding.phoneNumberEditText.text.toString(),
            email = binding.emailEditText.text.toString()
        )
        viewModel.validateAndUpdateContactInfo(contactInfo)
    }



    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.contentScrollView.isVisible = !isLoading
        binding.saveButton.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccessMessage(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = EditContactInfoDialogFragment()
    }
}