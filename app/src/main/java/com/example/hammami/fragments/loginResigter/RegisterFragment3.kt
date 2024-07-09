package com.example.hammami.fragments.loginResigter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentRegister3Binding
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment3 : Fragment() {
    private lateinit var binding: FragmentRegister3Binding
    private val viewModel: HammamiViewModel by viewModels()

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
            viewModel.registrationData.collect { data ->
                binding.apply {
                    textFieldPhoneNumber.editText?.setText(data.phoneNumber)
                    textFieldEmail.editText?.setText(data.email)
                }
            }
        }
    }

    private fun onBackButtonClick() {
        findNavController().popBackStack()
    }

    private fun onNextButtonClick() {
        val phoneNumberPattern = "^\\d{10}$".toRegex()


        val phoneNumber = ValidationUtil.validateAndReturnField(
            binding.textFieldPhoneNumber,
            getString(R.string.inserisci_il_numero_di_cellulare)
        ) { input: String -> phoneNumberPattern.matches(input) }

        val email = ValidationUtil.validateAndReturnField(
            binding.textFieldEmail,
            getString(R.string.err_email_obbligatoria),
            getString(R.string.err_email_non_valida)
        ) { input: String -> android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches() }

        if (phoneNumber != null && email != null) {
            viewModel.updateRegistrationData { currentData ->
                currentData.copy(
                    phoneNumber = phoneNumber,
                    email = email
                )
            }
            navigateToNextFragment()
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