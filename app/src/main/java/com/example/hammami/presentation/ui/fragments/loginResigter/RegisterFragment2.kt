package com.example.hammami.presentation.ui.fragments.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentRegister2Binding
import com.example.hammami.presentation.ui.fragments.BaseFragment
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class RegisterFragment2 : BaseFragment() {
    private val viewModel: RegisterViewModel by activityViewModels()
    private var _binding: FragmentRegister2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegister2Binding.inflate(inflater, container, false)
        return binding.root
    }

// RegisterFragment2.kt
override fun setupUI() {
    setupDatePicker()
    with(binding) {
        topAppBar.setNavigationOnClickListener {
            onBackClick()
        }
        buttonNext.setOnClickListener {
            viewModel.updateRegistrationState {currentState ->
                currentState.copy(
                    birthDate = textFieldBirthDate.editText?.text.toString(),
                    gender = textFieldGender.editText?.text.toString(),
                    allergies = textFieldAllergies.editText?.text.toString(),
                    disabilities = textFieldDisabilities.editText?.text.toString()
                )
            }
            viewModel.ValidateCurrentStep(RegistrationStep.HEALTH_INFO)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val initialState = viewModel.state.value
            textFieldBirthDate.editText?.setText(initialState.birthDate)
            textFieldGender.editText?.setText(initialState.gender)
            textFieldAllergies.editText?.setText(initialState.allergies)
            textFieldDisabilities.editText?.setText(initialState.disabilities)
        }

        // Osserva solo gli errori
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                updateFieldValidationUI(textFieldBirthDate, state.birthDateError)
                updateFieldValidationUI(textFieldGender, state.genderError)
            }
        }
    }
}
    private fun setupDatePicker() {
        binding.textFieldBirthDate.setEndIconOnClickListener { showDatePicker() }
        binding.textFieldBirthDate.editText?.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_birth_date))
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Date(selection)
            updateBirthDateField(date)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun updateBirthDateField(date: Date) {
        val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        binding.textFieldBirthDate.editText?.setText(formattedDate)
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvent.collectLatest { event ->
                handleUiEvent(event)
            }
        }
    }

    private fun handleUiEvent(event: RegisterViewModel.UiEvent) {
        when (event) {
            is RegisterViewModel.UiEvent.NavigateToNextStep -> findNavController().navigate(R.id.action_registerFragment2_to_registerFragment3)
            is RegisterViewModel.UiEvent.ShowError -> showSnackbar(event.error)
            else -> {} // Handle other events if needed
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}