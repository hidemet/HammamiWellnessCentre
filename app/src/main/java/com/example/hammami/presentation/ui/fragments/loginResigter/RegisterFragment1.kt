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
import com.example.hammami.databinding.FragmentRegister1Binding
import com.example.hammami.presentation.ui.fragments.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment1 : BaseFragment() {
    private val viewModel: RegisterViewModel by activityViewModels()
    private var _binding: FragmentRegister1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegister1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {

        with(binding) {
            topAppBar.setNavigationOnClickListener {
                onBackClick()
            }
            buttonNext.setOnClickListener {
                viewModel.updateRegistrationState { currentState ->
                    currentState.copy(
                        firstName = textFieldFirstName.editText?.text.toString(),
                        lastName = textFieldLastName.editText?.text.toString()
                    )
                }
                viewModel.ValidateCurrentStep(RegistrationStep.PERSONAL_INFO)
            }

            viewLifecycleOwner.lifecycleScope.launch{
                val initialState = viewModel.state.value
                textFieldFirstName.editText?.setText(initialState.firstName)
                textFieldLastName.editText?.setText(initialState.lastName)
            }

            // Osserva solo gli errori
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.state.collect { state ->
                    updateFieldValidationUI(textFieldFirstName, state.firstNameError)
                    updateFieldValidationUI(textFieldLastName, state.lastNameError)
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
            is RegisterViewModel.UiEvent.NavigateToNextStep -> findNavController().navigate(R.id.action_registerFragment1_to_registerFragment2)
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