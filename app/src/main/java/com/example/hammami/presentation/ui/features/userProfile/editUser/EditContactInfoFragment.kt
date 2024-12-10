package com.example.hammami.presentation.ui.features.userProfile.editUser

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentEditContactInfoBinding
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import com.example.hammami.presentation.ui.activities.UserProfileViewModel.UiEvent
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.util.hideKeyboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditContactInfoFragment : BaseFragment() {
    private var _binding: FragmentEditContactInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by activityViewModels()
    private var originalUserEmail: String? = null
    private var hasEmailChanged = false

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
    }


    private fun setupSaveButton() = with(binding) {
        saveButton.isEnabled = false
        saveButton.setOnClickListener { onSaveButtonClick() }
    }

    private fun hasChanges(): Boolean {
        val currentEmail = binding.emailEditText.text.toString()
        val currentPhoneNumber = binding.phoneNumberEditText.text.toString()
        val user = viewModel.uiState.value.user

        return user?.email != currentEmail || user?.phoneNumber != currentPhoneNumber
    }

    private fun updateSaveButtonVisibility() {
        binding.saveButton.isVisible = hasChanges()
    }


    private fun onSaveButtonClick() {
        hideKeyboard()
        val info = UserProfileViewModel.UserData.ContactInfoData(
            email = binding.emailEditText.text.toString(),
            phoneNumber = binding.phoneNumberEditText.text.toString()
        )

        if (hasEmailChanged) {
            showPasswordConfirmationDialog(newEmail)
        } else {
            updateContactInfo(newEmail)
        }
    }

    private fun showPasswordConfirmationDialog(newEmail: String) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_password, null)
        val passwordEditText =
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.passwordEditText)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_password)
            .setView(dialogView)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val password = passwordEditText.text.toString()
                // Qui dovresti chiamare un Use Case per la reimpostazione della password
                //  o un metodo nel ViewModel che gestisce l'aggiornamento dell'email con password.
                //  Per ora, simuliamo l'aggiornamento.
                updateContactInfo(newEmail)

            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.show()
    }

    private fun updateContactInfo(newEmail: String) {
        val updatedUser = viewModel.uiState.value.user?.copy(
            phoneNumber = binding.phoneNumberEditText.text.toString(),
            email = newEmail
        ) ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateUserData(updatedUser)
        }
        findNavController().navigateUp()
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        showLoading(state.isLoading)
                        // Gestisci altri aggiornamenti dell'interfaccia utente in base allo stato
                        state.userValidationError?.let { validationError ->
                            binding.emailInputLayout.error =
                                validationError.emailError?.asString(requireContext())
                            binding.phoneNumberInputLayout.error =
                                validationError.phoneNumberError?.asString(requireContext())
                        }
                    }
                }

                launch {
                    viewModel.updateEvents.collect { event ->
                        when (event) {
                            is UiEvent.ShowSnackbar -> showSnackbar(event.message)
                            // ... gestione di altri eventi ...
                        }
                    }
                }
            }
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