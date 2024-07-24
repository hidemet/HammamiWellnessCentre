package com.example.hammami.fragments.loginResigter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hammami.databinding.FragmentRegister3Binding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.models.RegistrationData
import com.example.hammami.util.StringValidators
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.LoginRegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment3 : BaseFragment() {
    private lateinit var binding: FragmentRegister3Binding
    private val viewModel: LoginRegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegister3Binding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboard()
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
            textFieldPhoneNumber.editText?.setText(data.phoneNumber)
            textFieldEmail.editText?.setText(data.email)
        }
    }



    private fun onNextButtonClick() {
        if (validateAllFields()) {
            updateRegistrationData()
            navigateToNextFragment()
        }
    }

    private fun validateAllFields(): Boolean {
        val isPhoneNumberValid =
            ValidationUtil.validateField(binding.textFieldPhoneNumber, StringValidators.PhoneNumber)
        val isEmailValid =
            ValidationUtil.validateField(binding.textFieldEmail, StringValidators.Email)

        return isPhoneNumberValid && isEmailValid
    }

    private fun updateRegistrationData() {
        val phoneNumber = binding.textFieldPhoneNumber.editText?.text.toString()
        val email = binding.textFieldEmail.editText?.text.toString()

        viewModel.updateRegistrationData { currentData ->
            currentData.copy(
                phoneNumber = phoneNumber,
                email = email
            )
        }
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(RegisterFragment3Directions.actionRegisterFragment3ToRegisterFragment4())
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}