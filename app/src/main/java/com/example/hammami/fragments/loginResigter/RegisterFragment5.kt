package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentRegister5Binding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.LoginRegisterViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "RegisterFragment5"
@AndroidEntryPoint
class RegisterFragment5 : BaseFragment() {
    private lateinit var binding: FragmentRegister5Binding
    private val viewModel: LoginRegisterViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegister5Binding.inflate(layoutInflater)
        return binding.root
    }


    override fun setupUI() {
        with(binding) {
            root.hideKeyboardOnOutsideTouch()
            buttonRegister.setOnClickListener { onButtonRegisterClick() }
            topAppBar.setNavigationOnClickListener { onBackClick() }
        }
    }

    override fun observeFlows() {
        viewModel.registrationState.collectResource(
            onSuccess = {
                showSnackbar(getString(R.string.registrazione_effettuata_con_successo))
                navigateToNextFragment()
            },
            onError = { showSnackbar(it ?: getString(R.string.errore_durante_la_registrazione)) }
        )
    }

    override fun showLoading(isLoading: Boolean) {
        binding.linearProgressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonRegister.isEnabled = !isLoading
    }


    private fun onButtonRegisterClick() {
        val currentData = viewModel.registrationData.value
        Log.d(TAG, "Registration data before creating user: $currentData")
        viewModel.createUser(currentData.email, currentData.password,currentData.toUser())
    }


    private fun navigateToNextFragment() {
        findNavController().navigate(RegisterFragment5Directions.actionRegisterFragment5ToLoginFragment())
    }

}