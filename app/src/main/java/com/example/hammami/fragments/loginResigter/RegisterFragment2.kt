package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentRegister2Binding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.models.RegistrationData
import com.example.hammami.util.StringValidators
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.RegisterViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class RegisterFragment2 : BaseFragment() {
    private var _binding: FragmentRegister2Binding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegister2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun setupUI() {
        binding.root.hideKeyboardOnOutsideTouch()
        setupClickListeners()
        setupDatePicker()
    }

    private fun setupClickListeners() {
        with(binding) {
            buttonNext.setOnClickListener { onNextButtonClick() }
            topAppBar.setNavigationOnClickListener { onBackClick() }
        }
    }

    private fun setupDatePicker() {
        binding.textFieldBirthDate.setEndIconOnClickListener {
            showDatePicker()
        }
        binding.textFieldBirthDate.editText?.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_birth_date))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(constraintsBuilder.build())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            val date = calendar.time
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
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registrationData.collect { data ->
                    updateUIWithRegistrationData(data)
                }
            }
        }
    }

    private fun updateUIWithRegistrationData(data: RegistrationData) {
        with(binding) {
            textFieldBirthDate.editText?.setText(data.birthDate)
            textFieldGender.editText?.setText(data.gender)
            textFieldAllergies.editText?.setText(data.allergies)
            textFieldDisabilities.editText?.setText(data.disabilities)
        }
    }

    private fun onNextButtonClick() {
        if (validateAllFields()) {
            updateRegistrationData()
            navigateToNextFragment()
        }
    }

    private fun validateAllFields(): Boolean {
        val isBirthDateValid = ValidationUtil.validateField(binding.textFieldBirthDate, StringValidators.NotBlank)
        val isGenderValid = ValidationUtil.validateField(binding.textFieldGender, StringValidators.NotBlank)

        return isBirthDateValid && isGenderValid
    }

    private fun updateRegistrationData() {
        with(binding) {
            val birthDate = textFieldBirthDate.editText?.text.toString()
            val gender = textFieldGender.editText?.text.toString()
            val allergies = textFieldAllergies.editText?.text.toString()
            val disabilities = textFieldDisabilities.editText?.text.toString()

            viewModel.updateRegistrationData { currentData ->
                currentData.copy(
                    birthDate = birthDate,
                    gender = gender,
                    allergies = allergies,
                    disabilities = disabilities
                )
            }
        }
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(RegisterFragment2Directions.actionRegisterFragment2ToRegisterFragment3())
    }
}