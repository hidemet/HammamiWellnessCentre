package com.example.hammami.fragments.loginResigter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hammami.databinding.FragmentRegister2Binding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.models.RegistrationData
import com.example.hammami.util.StringValidators
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment2 : BaseFragment() {
    private lateinit var binding: FragmentRegister2Binding
    private val viewModel: HammamiViewModel by activityViewModels()

    private lateinit var dayEditText: EditText
    private lateinit var monthEditText: EditText
    private lateinit var yearEditText: EditText
    private lateinit var genderEditText: EditText
    private lateinit var allergiesEditText: EditText
    private lateinit var disabilitiesEditText: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegister2Binding.inflate(layoutInflater)
        return binding.root
    }

    override fun setupUI() {
        with(binding) {
            root.hideKeyboardOnOutsideTouch()
            buttonNext.setOnClickListener { onNextButtonClick() }
            topAppBar.setNavigationOnClickListener { onBackClick() }

            dayEditText = textFieldDay.editText!!
            monthEditText = textFieldMonth.editText!!
            yearEditText = textFieldYear.editText!!
            genderEditText = textFieldGender.editText!!
            allergiesEditText = textFieldAllergies.editText!!
            disabilitiesEditText = textFieldDisabilities.editText!!
        }
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
        binding.apply {
            textFieldDay.editText?.setText(data.birthDate.split("/").getOrNull(0))
            textFieldMonth.editText?.setText(data.birthDate.split("/").getOrNull(1))
            textFieldYear.editText?.setText(data.birthDate.split("/").getOrNull(2))
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
        val isBirthDateValid = ValidationUtil.validateBirthDate(
            binding.textFieldDay,
            binding.textFieldMonth,
            binding.textFieldYear,
            binding.textViewDataError
        )
        val isGenderValid = ValidationUtil.validateField(binding.textFieldGender, StringValidators.NotBlank)

        return isBirthDateValid && isGenderValid
    }

    private fun updateRegistrationData() {
        val birthDate = "${dayEditText.text}/${monthEditText.text}/${yearEditText.text}"
        val gender = genderEditText.text.toString()
        val allergies = allergiesEditText.text.toString()
        val disabilities = disabilitiesEditText.text.toString()

        viewModel.updateRegistrationData { currentData ->
            currentData.copy(
                birthDate = birthDate,
                gender = gender,
                allergies = allergies,
                disabilities = disabilities
            )
        }
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(RegisterFragment2Directions.actionRegisterFragment2ToRegisterFragment3())
    }

}