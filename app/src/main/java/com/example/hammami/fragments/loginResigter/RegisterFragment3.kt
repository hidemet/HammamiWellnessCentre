package com.example.hammami.fragments.loginResigter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.activities.LoginRegisterActivity
import com.example.hammami.databinding.FragmentRegister3Binding
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel
import com.google.android.material.internal.ViewUtils.hideKeyboard


class RegisterFragment3 : Fragment() {
    private lateinit var binding: FragmentRegister3Binding
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
        binding = FragmentRegister3Binding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.hideKeyboardOnOutsideTouch()

        binding.buttonNext.setOnClickListener { onButtonNextClick() }
        binding.topAppBar.setNavigationOnClickListener { onToolbarBackClick() }

        viewModel.registrationData.observe(viewLifecycleOwner) { data ->
            binding.textFieldPhoneNumber.editText?.setText(data.phoneNumber)
            binding.textFieldEmail.editText?.setText(data.email)
        }



    }

    private fun onToolbarBackClick() {
        findNavController().popBackStack()
    }

    private fun onButtonNextClick() {
        val phoneNumberPattern = "^\\d{10}$".toRegex()


        val phoneNumber = ValidationUtil.validateAndReturnField(
            binding.textFieldPhoneNumber,
            "Inserisci il numero di cellulare"
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
        findNavController().navigate(R.id.action_registerFragment3_to_registerFragment4)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}