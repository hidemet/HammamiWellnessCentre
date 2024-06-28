package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentRegister1Binding
import com.example.hammami.viewmodel.HammamiViewModel
import com.google.android.material.textfield.TextInputLayout

class RegisterFragment1 : Fragment() {
    private lateinit var binding: FragmentRegister1Binding
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
        binding = FragmentRegister1Binding.inflate(layoutInflater)
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
        val firstName = validateAndReturnField(binding.textFieldFirstName, "Nome obbligatorio")
        val lastName = validateAndReturnField(binding.textFieldLastName, "Cognome obbligatorio")


        if (firstName != null && lastName != null) {
            viewModel.updateRegisterUserData("firstName", firstName)
            viewModel.updateRegisterUserData("lastName", lastName)
            findNavController().navigate(R.id.action_registerFragment1_to_registerFragment2)
        }
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