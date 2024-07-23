package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hammami.databinding.FragmentRegister4Binding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.models.RegistrationData
import com.example.hammami.util.ValidationResult
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment4 : BaseFragment() {
    private lateinit var binding: FragmentRegister4Binding
    private val viewModel: HammamiViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegister4Binding.inflate(layoutInflater)
        return binding.root
    }


    override fun setupUI() {
        with(binding) {
            root.hideKeyboardOnOutsideTouch()
            buttonNext.setOnClickListener { onNextButtonClick() }
            topAppBar.setNavigationOnClickListener { onBackClick() }
            setupPasswordVisibilityToggle()
            setupPasswordValidation()
        }
    }


  override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registrationData.collect { data ->
                updateUIWithRegistrationData(data)
            }
        }
    }

    private fun updateUIWithRegistrationData(data: RegistrationData) {
        binding.textFieldPassword.editText?.setText(data.password)
    }

    private fun onNextButtonClick() {
        if (validatePasswords()) {
            updateRegistrationData()
            navigateToNextFragment()
        }
    }

    private fun updateRegistrationData() {
        val password = binding.textFieldPassword.editText?.text.toString()
        viewModel.updateRegistrationData { currentData ->
            currentData.copy(password = password)
        }
    }

    private fun validatePasswords(): Boolean {
        val password = binding.textFieldPassword.editText?.text.toString()
        val confirmPassword = binding.textFieldConfirmPassword.editText?.text.toString()

        return when (val result = ValidationUtil.validatePasswords(password, confirmPassword)) {
            is ValidationResult.Valid -> {
                updateErrorState()
                true
            }
            is ValidationResult.Invalid -> {
                updateErrorState(result.errorMessage)
                false
            }
        }
    }

    private fun updateErrorState(errorMessage: String? = null) {
        binding.apply {
            textFieldPassword.error = if (errorMessage != null) " " else null
            textFieldConfirmPassword.error = errorMessage
        }
    }


    private fun setupPasswordVisibilityToggle() {
        binding.checkboxShowPassword.setOnCheckedChangeListener { _, isChecked ->
            val transformationMethod =
                if (isChecked) null else PasswordTransformationMethod.getInstance()
            binding.textFieldPassword.editText?.transformationMethod = transformationMethod
            binding.textFieldConfirmPassword.editText?.transformationMethod = transformationMethod
        }
    }

    private fun EditText.setupValidation(onTextChanged: (String) -> Unit) {
        this.doAfterTextChanged {
            onTextChanged(it.toString())
        }
    }

    private fun setupPasswordValidation() {
        binding.textFieldPassword.editText?.setupValidation { triggerValidation() }
        binding.textFieldConfirmPassword.editText?.setupValidation { triggerValidation() }
    }

    private fun triggerValidation() {
        validatePasswords()
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(RegisterFragment4Directions.actionRegisterFragment4ToRegisterFragment5())
    }
}