package com.example.hammami.presentation.ui.features.userProfile.editUser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.hammami.R
import com.example.hammami.databinding.DialogResetPasswordBinding
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPasswordDialogFragment : DialogFragment() {
    private var _binding: DialogResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

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
            viewModel.uiState.collect { state ->
                handleUiState(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collect { event ->
                handleEvent(event)
            }
        }
    }

    private fun handleUiState(state: UserProfileViewModel.UiState) {
        showLoading(state.isLoading)
        binding.emailInputLayout.error = state.validationState.emailError?.asString(requireContext())
    }

    private fun handleEvent(event: UserProfileViewModel.UiEvent) {
        when (event) {
            is UserProfileViewModel.UiEvent.ShowSnackbar -> {
                showSuccessMessage(event.message.asString(requireContext()))
                dismiss()
            }
            is UserProfileViewModel.UiEvent.AccountDeleted -> { /* Non gestito in questo dialog */ }
        else -> { /* Non gestito */ }
        }
    }

    private fun onConfirmClick() {
        val email = binding.emailEditText.text.toString()
        viewModel.onEvent(UserProfileViewModel.UserProfileEvent.ResetPassword(email))
    }




    private fun showLoading(isLoading: Boolean) {
        binding.linearProgressIndicator.isVisible = isLoading
        binding.confirmButton.isEnabled = !isLoading
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