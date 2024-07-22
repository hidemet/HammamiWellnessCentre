package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentRegister5Binding
import com.example.hammami.util.Resource
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.HammamiViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "RegisterFragment5"
@AndroidEntryPoint
class RegisterFragment5 : Fragment() {
    private lateinit var binding: FragmentRegister5Binding
    private val viewModel: HammamiViewModel by activityViewModels()


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

        setupUI()
        observeRegistration()
    }

    private fun setupUI() {
        with(binding) {
            root.hideKeyboardOnOutsideTouch()
            buttonRegister.setOnClickListener { onButtonRegisterClick() }
            topAppBar.setNavigationOnClickListener { onBackButtonClick() }
        }
    }


    private fun onBackButtonClick() {
        findNavController().popBackStack()
    }

    private fun onButtonRegisterClick() {
        val currentData = viewModel.registrationData.value
        Log.d(TAG, "Registration data before creating user: $currentData")
        viewModel.createUser(currentData.email, currentData.password,currentData.toUser())
    }
    private fun observeRegistration() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registrationState.collect { state ->
                handleResourceState(state,
                    onSuccess = {  showSnackbar(getString(R.string.registrazione_effettuata_con_successo))
                        navigateToNextFragment()
                    },
                    onError = { showSnackbar(it ?: getString(R.string.errore_durante_la_registrazione)) }
                )
            }
        }
    }

    private fun <T> handleResourceState(
        state: Resource<T>,
        onSuccess: (T) -> Unit,
        onError: (String?) -> Unit
    ) {
        when (state) {
            is Resource.Loading -> showLoading(true)
            is Resource.Success -> {
                showLoading(false)
                state.data?.let { onSuccess(it) }
            }

            is Resource.Error -> {
                showLoading(false)
                onError(state.message)
            }
            is Resource.Unspecified -> Unit
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.linearProgressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonRegister.isEnabled = !isLoading
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(RegisterFragment5Directions.actionRegisterFragment5ToLoginFragment())
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.ok)) { }
            .show()
    }

}