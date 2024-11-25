package com.example.hammami.presentation.ui.features.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.FragmentRegister1Binding
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment1 : BaseFragment() {
    private val viewModel: RegisterViewModel by activityViewModels()
    private var _binding: FragmentRegister1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegister1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        with(binding) {
            topAppBar.setNavigationOnClickListener { findNavController().navigateUp() }
            buttonNext.setOnClickListener { validateAndProceed() }
        }
    }



    override fun observeFlows() {
        viewModel.state.collectLatestLifecycleFlow { state ->
            binding.textFieldFirstName.editText?.setText(state.firstName)
            binding.textFieldLastName.editText?.setText(state.lastName)
            updateFieldValidationUI(binding.textFieldFirstName, state.firstNameError)
            updateFieldValidationUI(binding.textFieldLastName, state.lastNameError)
        }
    }

    private fun validateAndProceed() {
        val firstName = binding.textFieldFirstName.editText?.text.toString()
        val lastName = binding.textFieldLastName.editText?.text.toString()

        showLoading(true)
        viewModel.validateAndUpdateStep(
            RegistrationStep.PERSONAL_INFO,
            mapOf("firstName" to firstName, "lastName" to lastName)
        ).collectLatestLifecycleFlow { result ->
            showLoading(false)
            when (result) {
                is RegisterViewModel.ValidationResult.Success -> {
                    findNavController().navigate(R.id.action_registerFragment1_to_registerFragment2)
                }
                is RegisterViewModel.ValidationResult.Error -> {
                    result.errors.forEach { (field, error) ->
                        when (field) {
                            "firstName" -> updateFieldValidationUI(binding.textFieldFirstName, error)
                            "lastName" -> updateFieldValidationUI(binding.textFieldLastName, error)
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