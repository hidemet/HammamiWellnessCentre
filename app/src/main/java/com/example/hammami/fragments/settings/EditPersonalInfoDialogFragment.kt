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
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hammami.R
import com.example.hammami.databinding.DialogEditPersonalInfoBinding
import com.example.hammami.models.User
import com.example.hammami.util.Resource
import com.example.hammami.util.StringValidators
import com.example.hammami.util.hideKeyboard
import com.example.hammami.viewmodel.EditUserProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditPersonalInfoDialogFragment : DialogFragment() {

    private var _binding: DialogEditPersonalInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditUserProfileViewModel by viewModels()

    private var isDataModified = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
            window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("EditPersonalInfoDialog", "onViewCreated called")
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
            listOf(
                firstNameEditText, lastNameEditText, dayEditText, monthAutoCompleteTextView,
                yearEditText, genderAutoCompleteTextView, allergiesEditText, disabilitiesEditText
            )
                .forEach { it.addTextChangedListener { onTextChanged() } }
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
//                        user.let { updateUIWithUserData(it) }
//                    }
//                }
                launch {
                    viewModel.user.collectLatest { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                showLoading(true)
                            }
                            is Resource.Success -> {
                                resource.data?.let { updateUIWithUserData(it) }
                                showLoading(false)
                            }

                            is Resource.Error -> {
                                showError(
                                    resource.message ?: "Error fetching user data"
                                )
                            }

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

    private fun showSnackbarAndDismiss(message: String) {
        viewModel.fetchUserProfile()
        hideKeyboard()
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
        lifecycleScope.launch {
            delay(3000)
            dismiss()
        }
    }

    private fun updateUIWithUserData(user: User) {
        binding.apply {
            firstNameEditText.setText(user.firstName)
            lastNameEditText.setText(user.lastName)
            val (day, month, year) = user.birthDate.split("/")
            dayEditText.setText(day)
            yearEditText.setText(year)
            monthAutoCompleteTextView.setText(month, false)
            genderAutoCompleteTextView.setText(user.gender, false)
            allergiesEditText.setText(user.allergies)
            disabilitiesEditText.setText(user.disabilities)
        }
        isDataModified = false
        binding.saveButton.visibility = View.GONE
    }

    private fun onSaveButtonClick() {
        if (validateAllFields()) {
            val updatedUser = createUpdatedUser(viewModel.user.value.data!!)
            Log.d("EditUserProfile", "Saving updated user from dialog: $updatedUser")
            hideKeyboard()
            viewModel.updateUserProfile(updatedUser, requireContext())
            Log.d("EditUserProfile", "Called updateUserProfile")
        }
    }

    private fun validateAllFields(): Boolean {
        val isFirstNameValid =
            ValidationUtil.validateField(binding.firstNameInputLayout, StringValidators.NotBlank)
        val isLastNameValid =
            ValidationUtil.validateField(binding.lastNameInputLayout, StringValidators.NotBlank)
        val isBirthDateValid = ValidationUtil.validateBirthDate(
            binding.dayInputLayout,
            binding.monthInputLayout,
            binding.yearInputLayout,
            binding.dataErrorTextView
        )
        val isGenderValid =
            ValidationUtil.validateField(binding.genderTextInputLayout, StringValidators.NotBlank)

        return isFirstNameValid && isLastNameValid && isBirthDateValid && isGenderValid
    }

    private fun createUpdatedUser(currentUser: User): User {
        val birthDate =
            "${binding.dayEditText.text}/${binding.monthAutoCompleteTextView.text}/${binding.yearEditText.text}"
        return currentUser.copy(
            firstName = binding.firstNameEditText.text.toString(),
            lastName = binding.lastNameEditText.text.toString(),
            birthDate = birthDate,
            gender = binding.genderAutoCompleteTextView.text.toString(),
            allergies = binding.allergiesEditText.text.toString(),
            disabilities = binding.disabilitiesEditText.text.toString()
        )
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentScrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
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