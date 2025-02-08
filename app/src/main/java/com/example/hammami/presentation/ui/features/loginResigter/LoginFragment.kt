package com.example.hammami.presentation.ui.features.loginResigter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.BottomSheetResetPasswordBinding
import com.example.hammami.databinding.FragmentLoginBinding
import com.example.hammami.presentation.ui.activities.MainActivity
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.loginResigter.LoginViewModel.*
import com.example.hammami.core.utils.hideKeyboardOnOutsideTouch
import com.example.hammami.presentation.ui.activities.AdminActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()
    private var _bottomSheetDialog: BottomSheetDialog? = null
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
        setupClickListeners()
        setupTextChangeListeners()
        setupTouchHandling()
    }


    private fun setupClickListeners() = with(binding) {
        buttonLogin.setOnClickListener { viewModel.submitLogin() }
        buttonRegister.setOnClickListener { navigateToRegister() }
        buttonForgotPassword.setOnClickListener { showResetPasswordDialog() }
    }

    private fun setupTextChangeListeners() = with(binding) {
        textFieldEmail.editText?.doAfterTextChanged {
            viewModel.updateEmailField(it.toString())
        }
        textFieldPassword.editText?.doAfterTextChanged {
            viewModel.updatePasswordField(it.toString())
        }
    }

    private fun setupTouchHandling() {
        binding.root.hideKeyboardOnOutsideTouch()
    }


    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeState() }
                launch { observeUiEvents() }
            }
        }
    }

    private suspend fun observeState() {
        viewModel.state.collect { updateUI(it) }
    }

    private suspend fun observeUiEvents() {
        viewModel.uiEvent.collect { handleUiEvent(it) }
    }

    private fun updateUI(state: UiState) = with(binding) {
        showLoading(state.isLoading)
        textFieldEmail.error = state.emailError?.asString(requireContext())
        textFieldPassword.error = state.passwordError?.asString(requireContext())
        _bottomSheetBinding?.textFieldResetPasswordEmail?.error =
            state.resetPasswordEmailError?.asString(requireContext())
    }

    private fun handleUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowError -> showSnackbar(event.error)
            is UiEvent.NavigateToMainActivity -> navigateToMainActivity()
            is UiEvent.NavigateToAdminActivity -> navigateToAdminActivity()
            is UiEvent.ResetPasswordSuccess -> {
                showSnackbar(
                    UiText.StringResource(
                        R.string.l_email_per_reimpostare_la_passowrd_stata_inviata_a,
                        event.email
                    )
                )
                showLoading(false)
                _bottomSheetDialog?.dismiss()
            }
            is UiEvent.ResetPasswordError -> showSnackbar(event.error)
        }
    }

    override fun showLoading(isLoading: Boolean) = with(binding) {
        circularProgressIndicator.isVisible = isLoading
        buttonLogin.isEnabled = !isLoading
        buttonForgotPassword.isEnabled = !isLoading
        textFieldEmail.isEnabled = !isLoading
        textFieldPassword.isEnabled = !isLoading
    }

    private fun navigateToRegister() {
        findNavController().navigate(R.id.action_loginFragment_to_registerFragment1)
    }

    private fun navigateToMainActivity() {
        Intent(requireActivity(), MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }

    private fun navigateToAdminActivity() {
        Intent(requireActivity(), AdminActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }

    private fun showResetPasswordDialog() {
        _bottomSheetBinding = BottomSheetResetPasswordBinding.inflate(layoutInflater)
        _bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(bottomSheetBinding.root)
            setupResetPasswordDialog()
            setOnDismissListener {
                viewModel.resetPasswordDialogClosed()
            }
            show()
        }
    }

    private fun setupResetPasswordDialog() = with(bottomSheetBinding) {
        buttonCancel.setOnClickListener { _bottomSheetDialog?.dismiss() }
        buttonConfirm.setOnClickListener {
            val email = textFieldResetPasswordEmail.editText?.text.toString()
            viewModel.handlePasswordReset(email)
        }

        // Osserva lo stato del reset password
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                textFieldResetPasswordEmail.error =
                    state.resetPasswordEmailError?.asString(requireContext())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanupBottomSheet()
        _binding = null
        _bottomSheetBinding = null
    }

    private fun cleanupBottomSheet() {
        _bottomSheetDialog?.let {
            if (it.isShowing) it.dismiss()
        }
        _bottomSheetDialog = null
    }
}