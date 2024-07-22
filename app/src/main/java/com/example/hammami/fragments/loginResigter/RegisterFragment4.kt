package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hammami.databinding.FragmentRegister4Binding
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment4 : Fragment() {
    private lateinit var binding: FragmentRegister4Binding
    private val viewModel: HammamiViewModel by activityViewModels()

    private var passwordEntered = false
    private var confirmPasswordEntered = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegister4Binding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()

    }

    private fun setupUI(){
        with(binding){
            root.hideKeyboardOnOutsideTouch()
            buttonNext.setOnClickListener { onNextButtonClick() }
            topAppBar.setNavigationOnClickListener { onBackButtonClick() }
            setupPasswordVisibilityToggle()
            setupPasswordValidation()
        }
    }

    private fun observeViewModel(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registrationData.collect { data ->
                binding.textFieldPassword.editText?.setText(data.password)
            }
        }
    }

    private fun onBackButtonClick() {
        findNavController().popBackStack()
    }

    private fun onNextButtonClick() {
        val password = binding.textFieldPassword.editText?.text.toString()
        val confirmPassword = binding.textFieldConfirmPassword.editText?.text.toString()

        if (validatePasswords(password, confirmPassword)) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.updateRegistrationData { currentData ->
                    currentData.copy(password = password)
                }
                kotlinx.coroutines.delay(100)
                navigateToNextFragment()
            }
        }

    }

    private fun validatePasswords(password: String, confirmPassword: String): Boolean {
        val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$".toRegex()

        return when {
            password.length < 8 -> {
                showError("La password deve essere lunga almeno 8 caratteri")
                false
            }

            !passwordPattern.matches(password) -> {
                showError("La password deve contenere almeno una lettera e un numero")
                false
            }

            password != confirmPassword -> {
                showError("Le password non coincidono")
                false
            }

            else -> {
                hideError()
                true
            }
        }
    }

    private fun showError(message: String) {
        binding.apply {
            textFieldPassword.error = " "
            textFieldConfirmPassword.error = message
        }
    }

    private fun hideError() {
        binding.apply {
            textFieldPassword.error = null
            textFieldConfirmPassword.error = null
        }
    }

    private fun setupPasswordVisibilityToggle() {
        binding.checkboxShowPassword.setOnCheckedChangeListener { _, isChecked ->
            binding.textFieldPassword.editText?.let { editText ->
                editText.transformationMethod =
                    if (isChecked) null else PasswordTransformationMethod.getInstance()
                editText.setSelection(editText.text.length)
            }
        }
    }

fun EditText.setupValidation(onTextChanged: (String) -> Unit) {
    this.doAfterTextChanged {
        onTextChanged(it.toString())
    }
}

private fun setupPasswordValidation() {
    binding.textFieldPassword.editText?.setupValidation {
        passwordEntered = it.isNotEmpty()
        triggerValidation()
    }
    binding.textFieldConfirmPassword.editText?.setupValidation {
        confirmPasswordEntered = it.isNotEmpty()
        triggerValidation()
    }
}

private fun triggerValidation() {
    validatePasswords()
}

private fun validatePasswords() {
    val password = binding.textFieldPassword.editText?.text.toString()
    val confirmPassword = binding.textFieldConfirmPassword.editText?.text.toString()
    validatePasswords(password, confirmPassword)
}

    private fun navigateToNextFragment() {
        findNavController().navigate(RegisterFragment4Directions.actionRegisterFragment4ToRegisterFragment5())
    }
}