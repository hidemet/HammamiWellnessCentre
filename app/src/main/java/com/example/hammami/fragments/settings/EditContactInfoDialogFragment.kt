package com.example.hammami.fragments.settings

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hammami.R
import com.example.hammami.databinding.DialogEditContactInfoBinding
import com.example.hammami.databinding.DialogReauthenticationBinding
import com.example.hammami.models.User
import com.example.hammami.util.Resource
import com.example.hammami.util.StringValidators
import com.example.hammami.util.hideKeyboard
import com.example.hammami.viewmodel.EditUserProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditContactInfoDialogFragment : DialogFragment() {

    private var _binding: DialogEditContactInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditUserProfileViewModel by activityViewModels()

    private var isDataModified = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
            window?.requestFeature(Window.FEATURE_NO_TITLE)
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditContactInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        setupTextChangeListeners()
    }

    private fun setupUI() {
        with(binding) {
            saveButton.isEnabled = false
            saveButton.setOnClickListener { onSaveButtonClick() }
            topAppBar.setNavigationOnClickListener { dismiss() }
        }
    }

    private fun setupTextChangeListeners() {
        binding.apply {
            listOf(phoneNumberEditText, emailEditText).forEach {
                it.addTextChangedListener { onTextChanged() }
            }
        }
    }

    private fun onTextChanged() {
        if (!isDataModified) {
            isDataModified = true
            binding.saveButton.visibility = View.VISIBLE
        }
        binding.saveButton.isEnabled = true
    }


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                launch {
//                    viewModel.user.collectLatest { user ->
//                        user?.let { updateUIWithUserData(it) }
//                    }
//                }
                launch {
                    viewModel.user.collectLatest { resource ->
                        when (resource) {
                            is Resource.Success -> resource.data?.let { updateUIWithUserData(it) }
                            is Resource.Error -> showError(resource.message ?: "Error fetching user data")
                            else -> {}
                        }
                    }
                }
                launch {
                    viewModel.profileUpdateEvent.collectLatest { result ->
                        when (result) {
                            is EditUserProfileViewModel.ProfileUpdateResult.Success -> {
                                showSnackbarAndDismiss(result.message)
                            }
                            is EditUserProfileViewModel.ProfileUpdateResult.Error -> {
                                showError(result.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUIWithUserData(user: User) {
        binding.apply {
            phoneNumberEditText.setText(user.phoneNumber)
            emailEditText.setText(user.email)
        }
        isDataModified = false
        binding.saveButton.visibility = View.GONE
    }

    private fun onSaveButtonClick() {
        if (validateFields()) {
            hideKeyboard()
            viewModel.user.value.let { currentUser ->
                val updatedUser = currentUser.data?.let { createUpdatedUser(it) }
                if (updatedUser != null) {
                    if (updatedUser.email != currentUser.data.email) {
                        showReAuthDialog(updatedUser)
                    } else {
                        updateProfile(updatedUser)
                    }
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        val isPhoneNumberValid = ValidationUtil.validateField(
            binding.phoneNumberInputLayout,
            StringValidators.PhoneNumber
        )
        val isEmailValid = ValidationUtil.validateField(
            binding.emailInputLayout,
            StringValidators.Email
        )
        return isPhoneNumberValid && isEmailValid
    }

    private fun createUpdatedUser(currentUser: User): User {
        return currentUser.copy(
            phoneNumber = binding.phoneNumberEditText.text.toString(),
            email = binding.emailEditText.text.toString()
        )
    }

    private fun showReAuthDialog(updatedUser: User) {
        val dialogView = DialogReauthenticationBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reauthentication_required)
            .setView(dialogView.root)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val password = dialogView.passwordEditText.text.toString()
                if (password.isNotEmpty()) {
                    updateProfile(updatedUser, password)
                } else {
                    showError(getString(R.string.password_required))
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun updateProfile(updatedUser: User, currentPassword: String? = null) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateUserProfile(updatedUser, requireContext(), currentPassword)
        }
    }

    private fun showSnackbarAndDismiss(message: String) {
        viewModel.fetchUserProfile()
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
        lifecycleScope.launch {
            delay(3000)
            dismiss()
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}