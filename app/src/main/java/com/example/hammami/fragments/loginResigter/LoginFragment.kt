package com.example.hammami.fragments.loginResigter

import ValidationUtil
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.activities.LoginRegisterActivity
import com.example.hammami.activities.StartActivity
import com.example.hammami.databinding.FragmentLoginBinding
import com.example.hammami.util.Resource
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: HammamiViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (activity as LoginRegisterActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.hideKeyboardOnOutsideTouch()

        binding.buttonLogin.setOnClickListener { onLoginClick() }
        binding.buttonRegister.setOnClickListener { onRegisterClick() }

        setupPasswordVisibilityToggle()
        observeLoginState()
    }



    private fun observeLoginState() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    showLoading(true)
                }

                is Resource.Success -> {
                    showLoading(false)
                    navigateToHome()
                }

                is Resource.Error -> {
                    showLoading(false)
                    showSnackbar(state.message ?: "Errore durante il login")
                }
            }
        }
    }

    private fun setupPasswordVisibilityToggle() {
        binding.checkboxShowPassword.setOnCheckedChangeListener { _, isChecked ->
            val method = if (isChecked) null else PasswordTransformationMethod.getInstance()
            binding.textFieldPassword.editText?.transformationMethod = method
        }
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).apply {
                setAction("OK") { dismiss() }
                show()
            }
        }
    }

    private fun onLoginClick() {
        val email = ValidationUtil.validateAndReturnField(
            binding.textFieldEmail,
            getString(R.string.err_email_obbligatoria),
            getString(R.string.err_email_non_valida)
        ) { input: String -> android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches() }
        val password = ValidationUtil.validateAndReturnField(
            binding.textFieldPassword,
            getString(R.string.err_password_obbligatoria),
            getString(R.string.err_password_corta)
        ) { input: String -> input.length >= 6 }

        if (email != null && password != null)
            viewModel.loginUser(email, password)
    }

    private fun onRegisterClick() {
        findNavController().navigate(R.id.action_loginFragment_to_registerFragment1)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.circularProgressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
    }

    private fun navigateToHome() {
        val intent = Intent(activity, StartActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}