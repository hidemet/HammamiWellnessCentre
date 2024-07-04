package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hammami.activities.LoginRegisterActivity
import com.example.hammami.databinding.FragmentRegister2Binding
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel


class RegisterFragment2 : Fragment() {
    private lateinit var binding: FragmentRegister2Binding
    private lateinit var viewModel: HammamiViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (activity as LoginRegisterActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegister2Binding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.hideKeyboardOnOutsideTouch()

        binding.buttonNext.setOnClickListener { onButtonNextClick() }
        binding.topAppBar.setNavigationOnClickListener { onToolbarBackClick() }

        viewModel.registrationData.observe(viewLifecycleOwner) { data ->
            binding.textFieldDay.editText?.setText(data.birthDate)
            binding.textFieldGender.editText?.setText(data.gender)
            binding.textFieldAllergies.editText?.setText(data.allergies)
            binding.textFieldDisabilities.editText?.setText(data.disabilities)
        }
    }

    private fun onToolbarBackClick() {
        findNavController().popBackStack()
    }

    private fun onButtonNextClick() {
        val birthDate = validateBirthDate()
        val gender =
            ValidationUtil.validateAndReturnField(binding.textFieldGender, "Seleziona il genere")
        val allergies = binding.textFieldAllergies.editText?.text.toString()
        val disabilities = binding.textFieldDisabilities.editText?.text.toString()

        if (birthDate != null && gender != null) {
            viewModel.updateRegistrationData { currentData ->
                currentData.copy(
                    birthDate = birthDate,
                    gender = gender,
                    allergies = allergies,
                    disabilities = disabilities
                )
            }
            navigateToNextFragment()
        }
    }

    private fun validateBirthDate(): String? {
        val day = binding.textFieldDay.editText?.text.toString()
        val month = binding.textFieldMonth.editText?.text.toString()
        val year = binding.textFieldYear.editText?.text.toString()
        val birthDate = "$day/$month/$year"

        return when {
            day.isBlank() || month.isBlank() || year.isBlank() -> {
                showError("Inserisci una data di nascita completa\n")
                null
            }

            !isValidDate(day, year) -> {
                showError("Inserisci una data valida")
                null
            }

            else -> {
                hideError()
                birthDate
            }
        }
    }

    private fun hideError() {
        binding.textViewDataError.visibility = View.GONE
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(RegisterFragment2Directions.actionRegisterFragment2ToRegisterFragment3())
    }

    private fun showError(errorMessage: String) {
        binding.apply {
            textFieldDay.error = ""
            textFieldMonth.error = ""
            textFieldYear.error = ""
            textViewDataError.text = errorMessage
            textViewDataError.visibility = View.VISIBLE
        }
    }

    private fun isValidDate(day: String, year: String): Boolean {
        val dayInt = day.toIntOrNull()
        val yearInt = year.toIntOrNull()
        return yearInt in 1..9999 && dayInt in 1..31
    }
}