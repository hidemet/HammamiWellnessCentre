package com.example.hammami.fragments.loginResigter

import ValidationUtil.validateAndReturnField
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentRegister2Binding
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment2 : Fragment() {
    private lateinit var binding: FragmentRegister2Binding
    private val viewModel: HammamiViewModel by viewModels()


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

        setupUI()
        observeViewModel()

    }


    private fun setupUI() {
        with(binding) {
            root.hideKeyboardOnOutsideTouch()
            buttonNext.setOnClickListener { onNextButtonClick() }
            topAppBar.setNavigationOnClickListener { onBackButtonClick() }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registrationData.collect { data ->
                    binding.apply {
                        textFieldDay.editText?.setText(data.birthDate.split("/").getOrNull(0))
                        textFieldMonth.editText?.setText(data.birthDate.split("/").getOrNull(1))
                        textFieldYear.editText?.setText(data.birthDate.split("/").getOrNull(2))
                        textFieldGender.editText?.setText(data.gender)
                        textFieldAllergies.editText?.setText(data.allergies)
                        textFieldDisabilities.editText?.setText(data.disabilities)
                    }
                }
            }
        }
    }


    private fun onBackButtonClick() {
        findNavController().popBackStack()
    }

    private fun onNextButtonClick() {
        val birthDate = validateBirthDate()
        val gender = validateAndReturnField(
            binding.textFieldGender,
            getString(R.string.seleziona_il_genere)
        )
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
                "$day/$month/$year"
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