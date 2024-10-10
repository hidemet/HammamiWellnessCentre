package com.example.hammami.presentation.ui.fragments.loginResigter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.BottomSheetResetPasswordBinding
import com.example.hammami.databinding.FragmentLoginBinding
import com.example.hammami.presentation.ui.activities.MainActivity
import com.example.hammami.presentation.ui.fragments.BaseFragment
import com.example.hammami.presentation.ui.fragments.loginResigter.LoginViewModel.*
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var _bottomSheetBinding: BottomSheetResetPasswordBinding? = null
    private val bottomSheetBinding get() = _bottomSheetBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        with(binding) {
            root.hideKeyboardOnOutsideTouch()
            buttonLogin.setOnClickListener { viewModel.onEvent(LoginFormEvent.Login) }
            buttonRegister.setOnClickListener { onRegisterClick() }
            buttonForgotPassword.setOnClickListener { showForgotPasswordDialog() }

            textFieldEmail.editText?.doAfterTextChanged {
                viewModel.onEvent(LoginFormEvent.EmailChanged(it.toString()))
            }
            textFieldPassword.editText?.doAfterTextChanged {
                viewModel.onEvent(LoginFormEvent.PasswordChanged(it.toString()))
            }
        }
    }


    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                updateUIWithState(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvent.collectLatest { event ->
                handleUiEvent(event)
            }
        }
    }

    private fun updateUIWithState(state: LoginFormState) {
        binding.textFieldEmail.error = state.emailError?.asString(requireContext())
        binding.textFieldPassword.error = state.passwordError?.asString(requireContext())
        _bottomSheetBinding?.let { bottomSheet ->
            bottomSheet.textFieldResetPasswordEmail.error =
                state.emailError?.asString(requireContext())
        }
    }

    private fun handleUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowError -> showSnackbar(event.error)
            is UiEvent.LoginSuccess -> navigateToMain()
            is UiEvent.ResetPasswordSuccess -> showSnackbar(
                UiText.StringResource(
                    R.string.l_email_per_reimpostare_la_passowrd_stata_inviata_a,
                    event.email
                )
            )
            is UiEvent.Loading -> showLoading(true)
            is UiEvent.Idle -> showLoading(false)

            else -> {}
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.circularProgressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
    }

    private fun onRegisterClick() {
        Log.d("LoginFragment", "Register button clicked")
        try {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment1)
            Log.d("LoginFragment", "Navigation to RegisterFragment1 successful")
        } catch (e: Exception) {
            Log.e("LoginFragment", "Navigation failed", e)
        }
    }

    private fun showForgotPasswordDialog() {
        _bottomSheetBinding = BottomSheetResetPasswordBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(_bottomSheetBinding!!.root)
            setupResetPasswordDialog(_bottomSheetBinding!!)
            setOnDismissListener {
                viewModel.resetPasswordDialogClosed()
            }
        }
        bottomSheetDialog.show()
    }
    private fun setupResetPasswordDialog(binding: BottomSheetResetPasswordBinding) {
        with(binding) {
            buttonCancel.setOnClickListener { bottomSheetDialog.dismiss() }
            buttonConfirm.setOnClickListener {
                val email = bottomSheetBinding.textFieldResetPasswordEmail.editText?.text.toString()
                viewModel.onEvent(LoginFormEvent.ResetPassword(email))
            }
            // Osserva lo stato del reset password
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.state.collect { state ->
                    textFieldResetPasswordEmail.error = state.resetPasswordEmailError?.asString(requireContext())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _bottomSheetBinding = null
        if (::bottomSheetDialog.isInitialized && bottomSheetDialog.isShowing) {
            bottomSheetDialog.dismiss()
        }
    }
}