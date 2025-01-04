package com.example.hammami.presentation.ui.features.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.FragmentRegistration4Binding
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationFragment4 : BaseFragment() {
    private val viewModel: RegisterViewModel by activityViewModels()
    private var _binding: FragmentRegistration4Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistration4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        with(binding) {
            topAppBar.setNavigationOnClickListener { onBackClick() }
            buttonNext.setOnClickListener { validateAndProceed() }
        }
    }

    override fun observeFlows() {
        viewModel.state.collectLatestLifecycleFlow { state ->
            binding.textFieldPassword.editText?.setText(state.password)
            binding.textFieldConfirmPassword.editText?.setText(state.confirmPassword)
            updateFieldValidationUI(binding.textFieldPassword, state.passwordError)
            updateFieldValidationUI(binding.textFieldConfirmPassword, state.confirmPasswordError)
        }
    }


    private fun validateAndProceed() {
        val password = binding.textFieldPassword.editText?.text.toString()
        val confirmPassword = binding.textFieldConfirmPassword.editText?.text.toString()

        showLoading(true)
        viewModel.validateAndUpdateStep(
            RegistrationStep.CREDENTIALS,
            mapOf("password" to password, "confirmPassword" to confirmPassword)
        ).collectLatestLifecycleFlow { result ->
            showLoading(false)
            when (result) {
                is RegisterViewModel.ValidationResult.Success -> {
                    findNavController().navigate(R.id.action_registerFragment4_to_registerFragment5)
                }

                is RegisterViewModel.ValidationResult.Error -> {
                    result.errors.forEach { (field, error) ->
                        when (field) {
                            "password" -> updateFieldValidationUI(binding.textFieldPassword, error)
                            "confirmPassword" -> updateFieldValidationUI(
                                binding.textFieldConfirmPassword,
                                error
                            )
                        }
                    }
                    showSnackbar(UiText.StringResource(R.string.please_correct_errors))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}