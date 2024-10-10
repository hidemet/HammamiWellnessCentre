package com.example.hammami.presentation.ui.fragments.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentRegister4Binding
import com.example.hammami.presentation.ui.fragments.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment4 : BaseFragment() {
    private val viewModel: RegisterViewModel by activityViewModels()
    private var _binding: FragmentRegister4Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegister4Binding.inflate(inflater, container, false)
        return binding.root
    }

    // RegisterFragment4.kt
    override fun setupUI() {
        with(binding) {
            topAppBar.setNavigationOnClickListener {
                onBackClick()
            }
            buttonNext.setOnClickListener {
                viewModel.updateRegistrationState { currentState ->
                    currentState.copy(
                        password = textFieldPassword.editText?.text.toString(),
                        confirmPassword = textFieldConfirmPassword.editText?.text.toString()
                    )
                }
                viewModel.ValidateCurrentStep(RegistrationStep.CREDENTIALS)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val initialState = viewModel.state.value
                textFieldPassword.editText?.setText(initialState.password)
                textFieldConfirmPassword.editText?.setText(initialState.confirmPassword)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.state.collect { state ->
                    updateFieldValidationUI(textFieldPassword, state.passwordError)
                    updateFieldValidationUI(textFieldConfirmPassword, state.confirmPasswordError)
                }
            }
        }
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvent.collectLatest { event ->
                handleUiEvent(event)
            }
        }
    }

    private fun handleUiEvent(event: RegisterViewModel.UiEvent) {
        when (event) {
            is RegisterViewModel.UiEvent.NavigateToNextStep -> findNavController().navigate(R.id.action_registerFragment4_to_registerFragment5)
            is RegisterViewModel.UiEvent.ShowError -> showSnackbar(event.error)
            is RegisterViewModel.UiEvent.Loading -> showLoading(true)
            is RegisterViewModel.UiEvent.Idle -> showLoading(false)
            else -> {} // Handle other events if needed
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}