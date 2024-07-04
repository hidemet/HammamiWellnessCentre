package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.activities.LoginRegisterActivity
import com.example.hammami.databinding.FragmentRegister4Binding
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel


class RegisterFragment4 : Fragment() {
    private lateinit var binding: FragmentRegister4Binding
    private lateinit var viewModel: HammamiViewModel

    private var passwordEntered = false
    private var confirmPasswordEntered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (activity as LoginRegisterActivity).viewModel
    }

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

        binding.root.hideKeyboardOnOutsideTouch()

        binding.buttonNext.setOnClickListener { onButtonNextClick() }
        binding.topAppBar.setNavigationOnClickListener { onToolbarBackClick() }

        setupPasswordVisibilityToggle()
        setupPasswordValidation()

        viewModel.registrationData.observe(viewLifecycleOwner) { data ->
            binding.textFieldPassword.editText?.setText(data.password)
        }
    }

    private fun onToolbarBackClick() {
        findNavController().popBackStack()
    }

    private fun onButtonNextClick() {
        val password = binding.textFieldPassword.editText?.text.toString()
        val confirmPassword = binding.textFieldConfirmPassword.editText?.text.toString()

        if (validatePasswords(password, confirmPassword)) {
            viewModel.updateRegistrationData { currentData ->
                currentData.copy(password = password)
            }
            navigateToNextFragment()
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
            val method = if (isChecked) null else PasswordTransformationMethod.getInstance()
            binding.textFieldPassword.editText?.transformationMethod = method
            binding.textFieldConfirmPassword.editText?.transformationMethod = method
        }
    }

    private fun setupPasswordValidation() {
        binding.textFieldPassword.editText?.doAfterTextChanged {
            if (!passwordEntered && it.toString().isNotEmpty()) {
                passwordEntered = true
            }
            triggerValidation()
        }
        binding.textFieldConfirmPassword.editText?.doAfterTextChanged {
            if (!confirmPasswordEntered && it.toString().isNotEmpty()) {
                confirmPasswordEntered = true
            }
            triggerValidation()
        }
    }

    private fun triggerValidation() {
        if (passwordEntered && confirmPasswordEntered) {
            validatePasswords()
        }
    }

    private fun validatePasswords() {
        val password = binding.textFieldPassword.editText?.text.toString()
        val confirmPassword = binding.textFieldConfirmPassword.editText?.text.toString()
        if (passwordEntered && confirmPasswordEntered) {
            validatePasswords(password, confirmPassword)
        }
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(R.id.action_registerFragment4_to_registerFragment5)
    }
}