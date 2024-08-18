package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hammami.databinding.FragmentRegister3Binding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.models.RegistrationData
import com.example.hammami.util.StringValidators
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment3 : BaseFragment() {
    private var _binding: FragmentRegister3Binding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegister3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun setupUI() {
        binding.root.hideKeyboardOnOutsideTouch()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        with(binding) {
            buttonNext.setOnClickListener { onNextButtonClick() }
            topAppBar.setNavigationOnClickListener { onBackClick() }
        }
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registrationData.collect { data ->
                    updateUIWithRegistrationData(data)
                }
            }
        }
    }

    private fun updateUIWithRegistrationData(data: RegistrationData) {
        with(binding) {
            textFieldPhoneNumber.editText?.setText(data.phoneNumber)
            textFieldEmail.editText?.setText(data.email)
        }
    }

    private fun onNextButtonClick() {
        if (validateAllFields()) {
            updateRegistrationData()
            navigateToNextFragment()
        }
    }

    private fun validateAllFields(): Boolean {
        val isPhoneNumberValid = ValidationUtil.validateField(binding.textFieldPhoneNumber, StringValidators.PhoneNumber)
        val isEmailValid = ValidationUtil.validateField(binding.textFieldEmail, StringValidators.Email)
        return isPhoneNumberValid && isEmailValid
    }

    private fun updateRegistrationData() {
        with(binding) {
            val phoneNumber = textFieldPhoneNumber.editText?.text.toString()
            val email = textFieldEmail.editText?.text.toString()

            viewModel.updateRegistrationData { currentData ->
                currentData.copy(
                    phoneNumber = phoneNumber,
                    email = email
                )
            }
        }
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(RegisterFragment3Directions.actionRegisterFragment3ToRegisterFragment4())
    }
}