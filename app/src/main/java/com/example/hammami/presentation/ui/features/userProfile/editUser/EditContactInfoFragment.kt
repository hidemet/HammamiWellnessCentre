package com.example.hammami.presentation.ui.features.userProfile.editUser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentEditContactInfoBinding
import com.example.hammami.presentation.ui.features.userProfile.UserProfileViewModel
import com.example.hammami.presentation.ui.features.userProfile.UserProfileViewModel.*
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.core.utils.hideKeyboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditContactInfoFragment : BaseFragment() {
    private var _binding: FragmentEditContactInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditContactInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        setupTopAppBar()
        setupFields()
        setupSaveButton()
    }

    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupFields() = with(binding) {
        viewModel.uiState.value.user?.let { user ->
            phoneNumberEditText.setText(user.phoneNumber)
            emailEditText.setText(user.email)
        }
        listOf(phoneNumberEditText, emailEditText).forEach {
            it.addTextChangedListener { binding.saveButton.isEnabled = hasChanges() }
        }
    }


    private fun setupSaveButton() = with(binding) {
        saveButton.isEnabled = false
        saveButton.setOnClickListener { onSaveButtonClick() }
    }

    private fun hasChanges(): Boolean {
        val currentEmail = binding.emailEditText.text.toString()
        val currentPhoneNumber = binding.phoneNumberEditText.text.toString()
        val user = viewModel.uiState.value.user

        return user?.email != currentEmail || user.phoneNumber != currentPhoneNumber
    }


    private fun onSaveButtonClick() {
        hideKeyboard()
        val email = binding.emailEditText.text.toString()
        val phone = binding.phoneNumberEditText.text.toString()
        val info = UserData.ContactInfoData(
            email = email,
            phoneNumber = phone
        )

        val user = viewModel.uiState.value.user ?: return

        if (user.email != email) {
            showPasswordConfirmationDialog(info)
        } else {
            viewModel.updateUserData(info)
            findNavController().navigateUp()
        }
    }

    private fun showPasswordConfirmationDialog(info: UserData.ContactInfoData) {
        val user = viewModel.uiState.value.user ?: return
        val oldEmail = user.email
        val message = getString(R.string.confirm_password_message, oldEmail)

        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_password, null)
        val passwordEditText =
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.passwordEditText)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_password)
            .setView(dialogView)
            .setPositiveButton(R.string.confirm, null) // Imposta il listener a null temporaneamente
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val password = passwordEditText.text.toString()
            if (password.isNotEmpty()) {
                viewModel.updateUserData(info, password)
                dialog.dismiss()
            } else {
                passwordEditText.error = getString(R.string.password_required)
            }
        }
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { observeUiState(it) } }
                launch { viewModel.uiEvents.collect { observeEvent(it) } }
            }
        }
    }

    private fun observeUiState(state: UiState) {
        showLoading(state.isLoading)
        state.userValidationError?.let { validationError ->
            binding.emailInputLayout.error = validationError.emailError?.asString(requireContext())
            binding.phoneNumberInputLayout.error = validationError.phoneNumberError?.asString(requireContext())
        }
    }

    private fun observeEvent(event: UiEvent) {
        when (event) {
            is UiEvent.UserMessage -> showSnackbar(event.message)
            else -> Unit
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = EditContactInfoFragment()
    }
}