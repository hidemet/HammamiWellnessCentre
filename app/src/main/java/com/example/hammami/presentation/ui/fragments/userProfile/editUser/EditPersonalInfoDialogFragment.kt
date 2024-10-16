package com.example.hammami.presentation.ui.fragments.userProfile.editUser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.hammami.R
import com.example.hammami.databinding.DialogEditPersonalInfoBinding
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import com.example.hammami.util.hideKeyboard
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditPersonalInfoDialogFragment : DialogFragment() {
    private var _binding: DialogEditPersonalInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogEditPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        with(binding) {
            topAppBar.setNavigationOnClickListener { dismiss() }
            saveButton.setOnClickListener { onSaveButtonClick() }
            birthDateEditText.setOnClickListener { showDatePicker() }

            viewModel.userState.value.let { state ->
                if (state is UserProfileViewModel.UserState.LoggedIn) {
                    val user = state.userData
                    firstNameEditText.setText(user.firstName)
                    lastNameEditText.setText(user.lastName)
                    birthDateEditText.setText(user.birthDate)
                    genderAutoCompleteTextView.setText(user.gender, false)
                    allergiesEditText.setText(user.allergies)
                    disabilitiesEditText.setText(user.disabilities)
                }
            }
        }

        setupTextChangeListeners()
    }

    private fun setupTextChangeListeners() {
        binding.apply {
            listOf(firstNameEditText, lastNameEditText, birthDateEditText,
                genderAutoCompleteTextView, allergiesEditText, disabilitiesEditText)
                .forEach { it.addTextChangedListener { onTextChanged() } }
        }
    }

    private fun onTextChanged() {
        binding.saveButton.isVisible = true
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.validationState.collect { state ->
                    updateUIWithErrors(state)
                }
            }
            launch {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }

    private fun updateUIWithErrors(state: UserProfileViewModel.ValidationState) {
        binding.apply {
            firstNameInputLayout.error = state.firstNameError?.asString(requireContext())
            lastNameInputLayout.error = state.lastNameError?.asString(requireContext())
            birthDateInputLayout.error = state.birthDateError?.asString(requireContext())
            genderTextInputLayout.error = state.genderError?.asString(requireContext())
        }
    }

    private fun handleUiState(state: UserProfileViewModel.UiState) {
        when (state) {
            is UserProfileViewModel.UiState.Success -> {
                showSuccessMessage(state.message.asString(requireContext()))
                dismiss()
            }
            is UserProfileViewModel.UiState.Error -> showError(state.message.asString(requireContext()))
            is UserProfileViewModel.UiState.Loading -> showLoading(true)
            else -> showLoading(false)
        }
    }

    private fun onSaveButtonClick() {
        hideKeyboard()
        val personalInfo = UserProfileViewModel.PersonalInfo(
            firstName = binding.firstNameEditText.text.toString(),
            lastName = binding.lastNameEditText.text.toString(),
            birthDate = binding.birthDateEditText.text.toString(),
            gender = binding.genderAutoCompleteTextView.text.toString(),
            allergies = binding.allergiesEditText.text.toString(),
            disabilities = binding.disabilitiesEditText.text.toString()
        )
        viewModel.validateAndUpdatePersonalInfo(personalInfo)
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Date(selection)
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.birthDateEditText.setText(format.format(date))
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.contentScrollView.isEnabled = !isLoading
        binding.saveButton.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccessMessage(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = EditPersonalInfoDialogFragment()
    }
}