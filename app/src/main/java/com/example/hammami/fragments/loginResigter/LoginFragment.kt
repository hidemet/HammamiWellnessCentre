package com.example.hammami.fragments.loginResigter

import ValidationUtil
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.activities.StartActivity
import com.example.hammami.databinding.BottomSheetForgotPasswordBinding
import com.example.hammami.databinding.FragmentLoginBinding
import com.example.hammami.util.Resource
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: HammamiViewModel by viewModels()
    private lateinit var bottomSheetDialog: BottomSheetDialog




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
        setupUI()
        observeViewModelStates()
    }

    private fun setupUI() {
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
            binding.textFieldPassword.editText?.let { editText ->
                editText.transformationMethod =
                    if (isChecked) null else PasswordTransformationMethod.getInstance()
                editText.setSelection(editText.text.length)
            }
        }
    }

    private fun observeViewModelStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                handleResourceState(state,
                    onSuccess = {navigateToNext() },
                    onError = { showSnackbar(it ?: getString(R.string.errore_durante_il_login)) }
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.resetPasswordState.collect { state ->
                handleResourceState(state,
                    onSuccess = {
                        showSnackbar(getString(R.string.la_password_stata_resettata_con_successo))
                    },
                    onError = {
                        showSnackbar(
                            it ?: getString(R.string.errore_durante_il_reset_della_password))
                    }
                )
            }
        }
    }

    private fun <T> handleResourceState(
        state: Resource<T>,
        onSuccess: (T) -> Unit,
        onError: (String?) -> Unit
    ) {
        when (state) {
            is Resource.Loading -> showLoading(true)
            is Resource.Success -> {
                showLoading(false)
                state.data?.let { onSuccess(it) }
            }

            is Resource.Error -> {
                showLoading(false)
                onError(state.message)
            }
            is Resource.Unspecified -> Unit
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

    private fun showForgotPasswordDialog() {
        bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            val bottomSheetBinding = BottomSheetForgotPasswordBinding.inflate(layoutInflater)
            setContentView(bottomSheetBinding.root)

            bottomSheetBinding.buttonCancel.setOnClickListener { dismiss() }
            bottomSheetBinding.buttonConfirm.setOnClickListener {
                val email = ValidationUtil.validateAndReturnField(
                    bottomSheetBinding.textFieldEmail,
                    getString(R.string.err_email_obbligatoria),
                    getString(R.string.err_email_non_valida)
                ) { input: String -> android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches() }

                if (email != null) {
                    viewModel.resetPassword(email)
                    dismiss()
                }
            }
        }
        bottomSheetDialog.show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.circularProgressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.ok)) { }
            .show()
    }

    private fun navigateToNext() {
        Intent(requireActivity(), StartActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }
}