package com.example.hammami.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.hammami.R
import com.example.hammami.databinding.DialogResetPasswordBinding
import com.example.hammami.util.Resource
import com.example.hammami.util.StringValidators
import com.example.hammami.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordDialogFragment : DialogFragment() {
    private var _binding: DialogResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogResetPasswordBinding.inflate(inflater, container, false)
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

    private fun setupUI() {
        with(binding) {
            buttonConfirm.setOnClickListener { onConfirmClick() }
            buttonCancel.setOnClickListener { dismiss() }
        }
    }

    private fun observeFlows() {
        viewModel.resetPasswordState.collect { result ->
            when (result) {
                is Resource.Success -> {
                    showLoading(false)
                    showSuccessMessage()
                    dismiss()
                }
                is Resource.Error -> {
                    showLoading(false)
                    showErrorMessage(result.message)
                }
                is Resource.Loading -> showLoading(true)
                else -> {}
            }
        }
    }

    private fun onConfirmClick() {
        val email = binding.textFieldEmail.editText?.text.toString()
        if (validateEmail(email)) {
            viewModel.resetPassword(email)
        }
    }

    private fun validateEmail(email: String): Boolean {
        return ValidationUtil.validateField(binding.textFieldEmail, StringValidators.Email)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.buttonConfirm.isEnabled = !isLoading
        binding.buttonCancel.isEnabled = !isLoading
        // Add a progress indicator to your layout and control its visibility here
    }

    private fun showSuccessMessage() {
        // Show a success message, possibly using a Snackbar or Toast
    }

    private fun showErrorMessage(message: String?) {
        // Show an error message, possibly using a Snackbar or Toast
    }

    companion object {
        fun newInstance() = ResetPasswordDialogFragment()
    }
}