package com.example.hammami.presentation.ui.features.userProfile.editUser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hammami.R
import com.example.hammami.databinding.DialogResetPasswordBinding
import com.example.hammami.presentation.ui.features.userProfile.UserProfileViewModel
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            confirmButton.setOnClickListener {
                val email = emailEditText.text.toString()
                viewModel.resetPassword(email)
            }
            topAppBar.setNavigationOnClickListener { dismiss() }

            emailEditText.addTextChangedListener { onTextChanged() }
        }
    }

    private fun onTextChanged() {
        binding.confirmButton.isEnabled = true
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { updateUI(it) } }
                launch { viewModel.uiEvents.collect { handleEvent(it) } }
            }
        }
    }

    private fun updateUI(state: UserProfileViewModel.UiState) {
        showLoading(state.isLoading)
        state.userValidationError?.let { validationError ->
            binding.emailInputLayout.error = validationError.emailError?.asString(requireContext())
        }
    }

    private fun handleEvent(event: UserProfileViewModel.UiEvent) {
        when (event) {
            is UserProfileViewModel.UiEvent.UserMessage -> {
                showSuccessMessage(event.message.asString(requireContext()))
                dismiss()
            }
            else -> Unit
        }
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