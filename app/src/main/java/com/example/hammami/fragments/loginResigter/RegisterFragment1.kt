package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hammami.databinding.FragmentRegister1Binding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.models.RegistrationData
import com.example.hammami.util.StringValidators
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment1 : BaseFragment() {
    private lateinit var binding: FragmentRegister1Binding
    private val viewModel: HammamiViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegister1Binding.inflate(layoutInflater)
        return binding.root
    }

    override fun setupUI() {
        with(binding) {
            root.hideKeyboardOnOutsideTouch()
            buttonNext.setOnClickListener { onNextButtonClick() }
            topAppBar.setNavigationOnClickListener { onBackClick() }
        }
    }


    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registrationData.collect { data ->
                updateUIWithRegistrationData(data)
            }
        }
    }

    private fun updateUIWithRegistrationData(data: RegistrationData) {
        binding.apply {
            textFieldFirstName.editText?.setText(data.firstName)
            textFieldLastName.editText?.setText(data.lastName)
        }
    }




    private fun onNextButtonClick() {
        if (validateAllFields()) {
            updateRegistrationData()
            navigateToNextFragment()
        }
    }

    private fun validateAllFields(): Boolean {
        val isFirstNameValid =
            ValidationUtil.validateField(binding.textFieldFirstName, StringValidators.NotBlank)
        val isLastNameValid =
            ValidationUtil.validateField(binding.textFieldLastName, StringValidators.NotBlank)

        return isFirstNameValid && isLastNameValid
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(RegisterFragment1Directions.actionRegisterFragment1ToRegisterFragment2())
    }


    private fun updateRegistrationData() {
        val firstName = binding.textFieldFirstName.editText?.text.toString()
        val lastName = binding.textFieldLastName.editText?.text.toString()

        viewModel.updateRegistrationData { currentData ->
            currentData.copy(
                firstName = firstName, lastName = lastName
            )
        }
    }


}