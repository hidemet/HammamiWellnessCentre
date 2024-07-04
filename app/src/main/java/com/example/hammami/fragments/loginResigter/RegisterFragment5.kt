package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.activities.LoginRegisterActivity
import com.example.hammami.databinding.FragmentRegister5Binding
import com.example.hammami.util.Resource
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel
import com.google.android.material.snackbar.Snackbar

private const val TAG = "RegisterFragment5"

class RegisterFragment5 : Fragment() {
    private lateinit var binding: FragmentRegister5Binding
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
        binding = FragmentRegister5Binding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.hideKeyboardOnOutsideTouch()

        binding.buttonRegister.setOnClickListener { onButtonRegisterClick() }
        binding.topAppBar.setNavigationOnClickListener { onToolbarBackClick() }

        observeRegistration()
    }

    private fun onToolbarBackClick() {
        findNavController().popBackStack()
    }

    private fun onButtonRegisterClick() {
        viewModel.createUser()
    }


    private fun observeRegistration() {
        viewModel.registrationState.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> showLoading(true)

                is Resource.Success -> {
                    showLoading(false)
                    showSnackbar("Login effettuato con successo")
                    navigateToNextFragment()
                }

                is Resource.Error -> {
                    showLoading(false)
                    showSnackbar(response.message ?: "Errore durante la registrazione")
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.linearProgressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonRegister.isEnabled = !isLoading
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(R.id.action_registerFragment5_to_loginFragment)
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).apply {
                setAction("OK") { dismiss() }
                show()
            }
        }
    }

}