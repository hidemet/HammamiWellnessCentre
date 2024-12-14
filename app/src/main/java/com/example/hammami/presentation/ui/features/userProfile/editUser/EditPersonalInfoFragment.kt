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
import com.example.hammami.databinding.FragmentEditPersonalInfoBinding // Nuovo binding
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import com.example.hammami.presentation.ui.activities.UserProfileViewModel.UiEvent
import com.example.hammami.presentation.ui.activities.UserProfileViewModel.UiState
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.util.hideKeyboard
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditPersonalInfoFragment : BaseFragment() {

    private var _binding: FragmentEditPersonalInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserProfileViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        setupAppBar()
        setupFields()
        setupSaveButton()
        setupDatePicker()
    }

    private fun setupDatePicker() {
        binding.birthDateEditText.setOnClickListener { showDatePicker() }
    }

    private fun setupAppBar() = with(binding) {
        topAppBar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupFields() = with(binding) {

        viewModel.uiState.value.user?.let { user ->
            firstNameEditText.setText(user.firstName)
            lastNameEditText.setText(user.lastName)
            birthDateEditText.setText(user.birthDate)
            genderAutoCompleteTextView.setText(user.gender, false)
            allergiesEditText.setText(user.allergies)
            disabilitiesEditText.setText(user.disabilities)
        }
    }


    private fun setupSaveButton() = with(binding) {
        listOf(
            binding.firstNameEditText,
            binding.lastNameEditText,
            binding.birthDateEditText,
            binding.genderAutoCompleteTextView,
            binding.allergiesEditText,
            binding.disabilitiesEditText
        ).forEach {
            it.addTextChangedListener { binding.saveButton.isEnabled = true }
        }

        saveButton.setOnClickListener {
            hideKeyboard()
            val info = UserProfileViewModel.UserData.PersonalInfoData(
                firstName = firstNameEditText.text.toString(),
                lastName = lastNameEditText.text.toString(),
                birthDate = binding.birthDateEditText.text.toString(),
                gender = binding.genderAutoCompleteTextView.text.toString(),
                allergies = binding.allergiesEditText.text.toString(),
                disabilities = binding.disabilitiesEditText.text.toString(),
            )
            viewModel.updateUserData(info)
            findNavController().navigateUp()

        }
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


    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { observeUiState(it) } }
                launch { viewModel.uiEvents.collect { observeEvent(it) } }
            }
        }
    }


    private fun observeEvent(event: UiEvent) {
        when (event) {
            is UiEvent.UserMessage -> {
                showSnackbar(event.message)
            }

            else -> Unit
        }
    }


    private fun observeUiState(state: UiState) {
        showLoading(state.isLoading)
        state.userValidationError?.let { validation ->
            with(binding) {
                firstNameInputLayout.error = validation.firstNameError?.asString(requireContext())
                lastNameInputLayout.error = validation.lastNameError?.asString(requireContext())
                birthDateInputLayout.error = validation.birthDateError?.asString(requireContext())
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}