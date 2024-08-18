package com.example.hammami.fragments.loginResigter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.activities.MainActivity
import com.example.hammami.databinding.BottomSheetResetPasswordBinding
import com.example.hammami.databinding.FragmentLoginBinding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.util.Resource
import com.example.hammami.util.StringValidators
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.LoginViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private var resetPasswordEmail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeFlows()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun setupUI() {
        with(binding) {
            root.hideKeyboardOnOutsideTouch()
            buttonLogin.setOnClickListener { onLoginClick() }
            buttonRegister.setOnClickListener { onRegisterClick() }
            buttonForgotPassword.setOnClickListener { showForgotPasswordDialog() }
        }
    }


    override fun observeFlows() {
        viewModel.loginState.collectResource(
            onSuccess = { navigateToMain() },
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
        binding.buttonRegister.isEnabled = !isLoading
        binding.buttonForgotPassword.isEnabled = !isLoading
    }

    private fun onLoginClick() {
        val email = binding.textFieldEmail.editText?.text.toString()
        val password = binding.textFieldPassword.editText?.text.toString()

        if (validateLoginInput(email, password)) {
            viewModel.loginUser(email, password)
        }
    }

    private fun validateLoginInput(email: String, password: String): Boolean {
        val isEmailValid = ValidationUtil.validateField(binding.textFieldEmail, StringValidators.Email)
        val isPasswordValid = ValidationUtil.validateField(binding.textFieldPassword, StringValidators.NotBlank)
        return isEmailValid && isPasswordValid
    }

    private fun onRegisterClick() {
        findNavController().navigate(R.id.action_loginFragment_to_registerFragment1)
    }

    private fun showForgotPasswordDialog() {
        val bottomSheetBinding = BottomSheetResetPasswordBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(bottomSheetBinding.root)
            setupResetPasswordDialog(bottomSheetBinding)
        }
        bottomSheetDialog.show()
    }

    private fun setupResetPasswordDialog(binding: BottomSheetResetPasswordBinding) {
        with(binding) {
            buttonCancel.setOnClickListener { bottomSheetDialog.dismiss() }
            buttonConfirm.setOnClickListener {
                val email = textFieldEmail.editText?.text.toString()
                if (ValidationUtil.validateField(textFieldEmail, StringValidators.Email)) {
                    viewModel.resetPassword(email)
                    resetPasswordEmail = email
                    bottomSheetDialog.dismiss()
                }
            }
        }
    }

    private fun navigateToMain() {
        Intent(requireActivity(), MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }
}