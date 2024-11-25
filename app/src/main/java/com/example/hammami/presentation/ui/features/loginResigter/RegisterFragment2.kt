package com.example.hammami.presentation.ui.features.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.FragmentRegister2Binding
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.loginResigter.RegisterViewModel.*
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
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

    override fun setupUI() {
        setupDatePicker()
        with(binding) {
            topAppBar.setNavigationOnClickListener { onBackClick() }
            buttonNext.setOnClickListener { validateAndProceed() }
        }
    }


    override fun observeFlows() {
        viewModel.state.collectLatestLifecycleFlow { state ->
            binding.textFieldBirthDate.editText?.setText(state.birthDate)
            binding.textFieldGender.editText?.setText(state.gender)
            binding.textFieldAllergies.editText?.setText(state.allergies)
            binding.textFieldDisabilities.editText?.setText(state.disabilities)
            updateFieldValidationUI(binding.textFieldBirthDate, state.birthDateError)
            updateFieldValidationUI(binding.textFieldGender, state.genderError)
        }
    }

    private fun validateAndProceed() {
        val birthDate = binding.textFieldBirthDate.editText?.text.toString()
        val gender = binding.textFieldGender.editText?.text.toString()
        val allergies = binding.textFieldAllergies.editText?.text.toString()
        val disabilities = binding.textFieldDisabilities.editText?.text.toString()

        showLoading(true)
        viewModel.validateAndUpdateStep(
            RegistrationStep.HEALTH_INFO,
            mapOf(
                "birthDate" to birthDate,
                "gender" to gender,
                "allergies" to allergies,
                "disabilities" to disabilities
            )
        ).collectLatestLifecycleFlow { result ->
            showLoading(false)
            when (result) {
                is ValidationResult.Success -> {
                    findNavController().navigate(R.id.action_registerFragment2_to_registerFragment3)
                }
                is ValidationResult.Error -> {
                    result.errors.forEach { (field, error) ->
                        when (field) {
                            "birthDate" -> updateFieldValidationUI(binding.textFieldBirthDate, error)
                            "gender" -> updateFieldValidationUI(binding.textFieldGender, error)
                        }
                    }
                    showSnackbar(UiText.StringResource(R.string.please_correct_errors))
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



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}