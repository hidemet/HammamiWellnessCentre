package com.example.hammami.fragments.loginResigter

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.activities.MainActivity
import com.example.hammami.databinding.BottomSheetForgotPasswordBinding
import com.example.hammami.databinding.FragmentLoginBinding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.util.StringValidators
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.LoginRegisterViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginRegisterViewModel by activityViewModels()
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private var resetPasswordEmail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun setupUI() {
        with(binding) {
            root.hideKeyboardOnOutsideTouch()
            buttonLogin.setOnClickListener { onLoginClick() }
            buttonRegister.setOnClickListener { onRegisterClick() }
            buttonForgotPassword.setOnClickListener { showForgotPasswordDialog() }
            setupPasswordVisibilityToggle()
        }
    }

    private fun setupPasswordVisibilityToggle() {
        binding.checkboxShowPassword.setOnCheckedChangeListener { _, isChecked ->
            val transformationMethod =
                if (isChecked) null else PasswordTransformationMethod.getInstance()
            binding.textFieldPassword.editText?.transformationMethod = transformationMethod
        }
    }

    override fun observeFlows() {
        viewModel.loginState.collectResource(
            onSuccess = { navigateToNext() },
            onError = { showSnackbar(it ?: getString(R.string.errore_durante_il_login)) }
        )

        viewModel.resetPasswordState.collectResource(
            onSuccess = {
                showSnackbar(
                    getString(
                        R.string.l_email_per_reimpostare_la_passowrd_stata_inviata_a,
                        resetPasswordEmail
                    )
                )
            },
            onError = {
                showSnackbar(
                    it ?: getString(R.string.errore_durante_il_reset_della_password)
                )
            }
        )
    }

    override fun showLoading(isLoading: Boolean) {
        binding.circularProgressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
    }

    private fun onLoginClick() {
        val isEmailValid = ValidationUtil.validateField(binding.textFieldEmail, StringValidators.Email)
        val isPasswordValid = ValidationUtil.validateField(binding.textFieldPassword, StringValidators.Password)

        if (isEmailValid && isPasswordValid) {
            val email = binding.textFieldEmail.editText?.text.toString()
            val password = binding.textFieldPassword.editText?.text.toString()
            viewModel.loginUser(email, password)
        }
    }

    private fun onRegisterClick() {
        findNavController().navigate(R.id.action_loginFragment_to_registerFragment1)
    }

    private fun showForgotPasswordDialog() {
        val bottomSheetBinding = BottomSheetForgotPasswordBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(bottomSheetBinding.root)

            bottomSheetBinding.buttonCancel.setOnClickListener { dismiss() }
            bottomSheetBinding.buttonConfirm.setOnClickListener {
                if (ValidationUtil.validateField(bottomSheetBinding.textFieldEmail, StringValidators.Email)) {
                    val email = bottomSheetBinding.textFieldEmail.editText?.text.toString()
                    viewModel.resetPassword(email)
                    resetPasswordEmail = email
                    dismiss()
                }
            }
        }
        bottomSheetDialog.show()
    }


    private fun navigateToNext() {
        Intent(requireActivity(), MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }
}