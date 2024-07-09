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
import com.example.hammami.databinding.FragmentRegister1Binding
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment1 : Fragment() {
    private lateinit var binding: FragmentRegister1Binding
    private val viewModel: HammamiViewModel by viewModels()


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
                    binding.textFieldFirstName.editText?.setText(data.firstName)
                    binding.textFieldLastName.editText?.setText(data.lastName)
                }
            }
        }
    }

    private fun onBackButtonClick() {
        viewModel.clearRegistrationData()
        findNavController().popBackStack()
    }

    private fun onNextButtonClick(){
        val firstName = validateAndReturnField(binding.textFieldFirstName, "Nome obbligatorio")
        val lastName = validateAndReturnField(binding.textFieldLastName, "Cognome obbligatorio")


        if (firstName != null && lastName != null) {

                updateRegistrationData(firstName,lastName)
                navigateToNextFragment()

        }
    }


    private fun navigateToNextFragment() {
        findNavController().navigate(R.id.action_registerFragment1_to_registerFragment2)
    }

    private fun updateRegistrationData(firstName: String, lastName: String) {
        viewModel.updateRegistrationData { currentData -> currentData.copy(firstName = firstName, lastName = lastName)}

    }


}