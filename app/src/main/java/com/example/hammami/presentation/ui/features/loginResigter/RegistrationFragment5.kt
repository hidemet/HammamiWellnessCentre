package com.example.hammami.presentation.ui.features.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.FragmentRegistration5Binding
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationFragment5 : BaseFragment() {
    private val viewModel: RegisterViewModel by activityViewModels()
    private var _binding: FragmentRegistration5Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistration5Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        with(binding) {
            topAppBar.setNavigationOnClickListener { onBackClick() }
            buttonRegister.setOnClickListener { validateAndRegister() }
        }
    }

    override fun observeFlows() {
        viewModel.state.collectLatestLifecycleFlow { state ->
            binding.textFieldFriendCode.editText?.setText(state.friendCode)
        }
    }

    private fun validateAndRegister() {
        val friendCode = binding.textFieldFriendCode.editText?.text.toString()

        showLoading(true)
        viewModel.validateAndUpdateStep(
            RegistrationStep.FRIEND_CODE,
            mapOf("friendCode" to friendCode)
        ).collectLatestLifecycleFlow { result ->
            when (result) {
                is RegisterViewModel.ValidationResult.Success -> {
                    performRegistration()
                }
                is RegisterViewModel.ValidationResult.Error -> {
                    showLoading(false)
                    result.errors["friendCode"]?.let { error ->
                        updateFieldValidationUI(binding.textFieldFriendCode, error)
                    }
                    showSnackbar(UiText.StringResource(R.string.please_correct_errors))
                }
            }
        }
    }


    private fun performRegistration() {
        viewModel.performRegistration(
            onSuccess = {
                showLoading(false)
                showSnackbar(UiText.StringResource(R.string.registration_successful))
                findNavController().navigate(R.id.action_registerFragment5_to_loginFragment)
            },
            onError = { error ->
                showLoading(false)
                showSnackbar(error)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}