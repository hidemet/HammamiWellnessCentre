package com.example.hammami.presentation.ui.fragments.userProfile.editUser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.hammami.databinding.DialogResetPasswordBinding
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPasswordDialogFragment : BottomSheetDialogFragment() {
    private var _binding: DialogResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        with(binding) {
            confirmButton.setOnClickListener { onConfirmClick() }
            topAppBar.setNavigationOnClickListener { dismiss() }

            emailEditText.addTextChangedListener { onTextChanged() }
        }
    }

    private fun onTextChanged() {
        binding.confirmButton.isEnabled = true
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.validationState.collect { state ->
                    binding.emailInputLayout.error = state.emailError?.asString(requireContext())
                }
            }
            launch {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
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

    private fun onConfirmClick() {
        val email = binding.emailEditText.text.toString()
        viewModel.resetPassword(email)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.confirmButton.isEnabled = !isLoading
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
        fun newInstance() = ResetPasswordDialogFragment()
    }
}