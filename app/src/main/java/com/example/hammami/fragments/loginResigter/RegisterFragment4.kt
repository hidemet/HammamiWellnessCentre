package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentRegister4Binding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.models.RegistrationData
import com.example.hammami.util.ValidationResult
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.RegisterViewModel
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment4 : BaseFragment() {
    private var _binding: FragmentRegister4Binding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegister4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun setupUI() {
        binding.root.hideKeyboardOnOutsideTouch()
        setupClickListeners()
        setupPasswordToggles()
    }

    private fun setupClickListeners() {
        with(binding) {
            buttonNext.setOnClickListener { onNextButtonClick() }
            topAppBar.setNavigationOnClickListener { onBackClick() }
        }
    }

    private fun setupPasswordToggles() {
        setupPasswordVisibilityToggle(binding.textFieldPassword)
        setupPasswordVisibilityToggle(binding.textFieldConfirmPassword)
    }

    private fun setupPasswordVisibilityToggle(textInputLayout: TextInputLayout) {
        textInputLayout.setEndIconOnClickListener {
            togglePasswordVisibility(textInputLayout)
        }

        textInputLayout.editText?.doAfterTextChanged {
            updatePasswordVisibilityIcon(textInputLayout)
        }
    }

    private fun togglePasswordVisibility(textInputLayout: TextInputLayout) {
        val editText = textInputLayout.editText ?: return
        val selection = editText.selectionEnd

        if (editText.transformationMethod is PasswordTransformationMethod) {
            editText.transformationMethod = null
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
        }

        editText.setSelection(selection)
        updatePasswordVisibilityIcon(textInputLayout)
    }

    private fun updatePasswordVisibilityIcon(textInputLayout: TextInputLayout) {
        val editText = textInputLayout.editText ?: return
        val isPasswordVisible = editText.transformationMethod == null

        val iconResId = if (isPasswordVisible) {
            R.drawable.ic_visibility
        } else {
            R.drawable.ic_visibility_off
        }

        textInputLayout.setEndIconDrawable(iconResId)
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
        binding.textFieldPassword.editText?.setText(data.password)
    }

    private fun onNextButtonClick() {
        if (validatePasswords()) {
            updateRegistrationData()
            navigateToNextFragment()
        }
    }

    private fun validatePasswords(): Boolean {
        val password = binding.textFieldPassword.editText?.text.toString()
        val confirmPassword = binding.textFieldConfirmPassword.editText?.text.toString()

        return when (val result = ValidationUtil.validatePasswords(password, confirmPassword)) {
            is ValidationResult.Valid -> {
                clearErrorState()
                true
            }
            is ValidationResult.Invalid -> {
                updateErrorState(result.errorMessage)
                false
            }
        }
    }

    private fun updateErrorState(errorMessage: String) {
        binding.apply {
            textFieldPassword.error = " "
            textFieldConfirmPassword.error = errorMessage
        }
    }

    private fun clearErrorState() {
        binding.apply {
            textFieldPassword.error = null
            textFieldConfirmPassword.error = null
        }
    }

    private fun updateRegistrationData() {
        val password = binding.textFieldPassword.editText?.text.toString()
        viewModel.updateRegistrationData { currentData ->
            currentData.copy(password = password)
        }
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(RegisterFragment4Directions.actionRegisterFragment4ToRegisterFragment5())
    }
}