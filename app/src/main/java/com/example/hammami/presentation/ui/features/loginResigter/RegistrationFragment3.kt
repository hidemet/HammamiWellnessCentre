package com.example.hammami.presentation.ui.features.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.FragmentRegistration3Binding
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.loginResigter.RegisterViewModel.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationFragment3 : BaseFragment() {
    private val viewModel: RegisterViewModel by activityViewModels()
    private var _binding: FragmentRegistration3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistration3Binding.inflate(inflater, container, false)
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
            binding.textFieldPhoneNumber.editText?.setText(state.phoneNumber)
            binding.textFieldEmail.editText?.setText(state.email)
            updateFieldValidationUI(binding.textFieldPhoneNumber, state.phoneNumberError)
            updateFieldValidationUI(binding.textFieldEmail, state.emailError)
        }
    }

    private fun validateAndProceed() {
        val phoneNumber = binding.textFieldPhoneNumber.editText?.text.toString()
        val email = binding.textFieldEmail.editText?.text.toString()

        showLoading(true)
        viewModel.validateAndUpdateStep(
            RegistrationStep.CONTACT_INFO,
            mapOf("phoneNumber" to phoneNumber, "email" to email)
        ).collectLatestLifecycleFlow { result ->
            showLoading(false)
            when (result) {
                is ValidationResult.Success -> {
                    findNavController().navigate(R.id.action_registerFragment3_to_registerFragment4)
                }
                is ValidationResult.Error -> {
                    result.errors.forEach { (field, error) ->
                        when (field) {
                            "phoneNumber" -> updateFieldValidationUI(binding.textFieldPhoneNumber, error)
                            "email" -> updateFieldValidationUI(binding.textFieldEmail, error)
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