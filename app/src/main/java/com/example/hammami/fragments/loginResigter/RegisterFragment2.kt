package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentRegister2Binding
import com.example.hammami.viewmodel.HammamiViewModel
import com.google.android.material.textfield.TextInputLayout

class RegisterFragment2 : Fragment() {
    private lateinit var binding: FragmentRegister2Binding
    private lateinit var viewModel: HammamiViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = defaultViewModelProviderFactory.create(HammamiViewModel::class.java)
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

        binding.buttonNext.setOnClickListener { onButtonNextClick() }
        binding.topAppBar.setNavigationOnClickListener { onToolbarBackClick() }
    }

    private fun onToolbarBackClick() {
        viewModel.clearRegisterUserData()
        findNavController().popBackStack()
    }

    private fun onButtonNextClick() {
        val birthDate = validateBirthDate()
        val gender = validateAndReturnField(binding.textFieldGender, "Genere obbligatorio")
        val allergies = binding.textFieldAllergies.editText?.text.toString()
        val disabilities = binding.textFieldDisabilities.editText?.text.toString()


        if (birthDate != null && gender != null) {
            viewModel.updateRegisterUserData("birthDate", birthDate)
            viewModel.updateRegisterUserData("gender", gender)
            viewModel.updateRegisterUserData("allergies", allergies)
            viewModel.updateRegisterUserData("disabilities", disabilities)
            // Navigate to the next fragment (you'll need to create the appropriate action in your nav graph)
            // findNavController().navigate(R.id.action_registerFragment2_to_registerFragment3)
        }
    }

    private fun validateBirthDate(): String? {
        val day = binding.textFieldDay.editText?.text.toString()
        val month = binding.textFieldMonth.editText?.text.toString()
        val year = binding.textFieldYear.editText?.text.toString()

        if (day.isBlank() || month.isBlank() || year.isBlank()) {
            binding.textFieldDay.error = "Inserisci una data di nascita completa"
            binding.textFieldMonth.error = "Inserisci una data di nascita completa"
            binding.textFieldYear.error = "Inserisci una data di nascita completa"
            return null
        }

        // Here you should add more sophisticated date validation
        return "$day/$month/$year"
    }

    private fun validateAndReturnField(
        field: TextInputLayout,
        emptyError: String,
        invalidError: String? = null,
        validation: ((String) -> Boolean)? = null
    ): String? {
        val text = field.editText?.text.toString()
        var error: String? = null
        when {
            text.isBlank() -> error = emptyError
            validation != null && !validation(text) -> error = invalidError
        }
        field.error = error
        return text.takeIf { error == null }
    }

}