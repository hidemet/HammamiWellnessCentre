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
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.FragmentRegister5Binding
import com.example.hammami.presentation.ui.fragments.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment5 : BaseFragment() {
    private val viewModel: RegisterViewModel by activityViewModels()
    private var _binding: FragmentRegister5Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegister5Binding.inflate(inflater, container, false)
        return binding.root
    }

    // RegisterFragment5.kt
    override fun setupUI() {

        with(binding) {
            topAppBar.setNavigationOnClickListener {
                onBackClick()
            }
            buttonRegister.setOnClickListener {
                viewModel.updateRegistrationState {currentState ->
                    currentState.copy(
                        friendCode = textFieldFriendCode.editText?.text.toString()
                    )
                }
                viewModel.ValidateCurrentStep(RegistrationStep.FRIEND_CODE)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.state.collect { state ->
                    textFieldFriendCode.editText?.setText(state.friendCode)
                    // TODO: gestire i casi d'errore per il friendCode
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
            is RegisterViewModel.UiEvent.RegistrationSuccess -> {
                showSnackbar(UiText.StringResource(R.string.registration_successful))
                // Navigate to login or main screen after successful registration
                findNavController().navigate(R.id.action_registerFragment5_to_loginFragment)
            }

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