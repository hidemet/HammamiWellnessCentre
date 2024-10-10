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
import com.example.hammami.databinding.FragmentRegister3Binding
import com.example.hammami.presentation.ui.fragments.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment3 : BaseFragment() {
    private val viewModel: RegisterViewModel by activityViewModels()
    private var _binding: FragmentRegister3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegister3Binding.inflate(inflater, container, false)
        return binding.root
    }

// RegisterFragment3.kt
override fun setupUI() {
    with(binding) {
        topAppBar.setNavigationOnClickListener {
            onBackClick()
        }
        buttonNext.setOnClickListener {
            viewModel.updateRegistrationState { currentState ->
                currentState.copy(
                    phoneNumber = textFieldPhoneNumber.editText?.text.toString(),
                    email = textFieldEmail.editText?.text.toString()
                )
            }
            viewModel.ValidateCurrentStep(RegistrationStep.CONTACT_INFO)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val initialState = viewModel.state.value
            textFieldPhoneNumber.editText?.setText(initialState.phoneNumber)
            textFieldEmail.editText?.setText(initialState.email)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                updateFieldValidationUI(textFieldPhoneNumber, state.phoneNumberError)
                updateFieldValidationUI(textFieldEmail, state.emailError)
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
            is RegisterViewModel.UiEvent.NavigateToNextStep -> findNavController().navigate(R.id.action_registerFragment3_to_registerFragment4)
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