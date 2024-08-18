package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentRegister5Binding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.util.hideKeyboardOnOutsideTouch
import com.example.hammami.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment5 : BaseFragment() {
    private var _binding: FragmentRegister5Binding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegister5Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun setupUI() {
        binding.root.hideKeyboardOnOutsideTouch()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        with(binding) {
            buttonRegister.setOnClickListener { onButtonRegisterClick() }
            topAppBar.setNavigationOnClickListener { onBackClick() }
        }
    }

    override fun observeFlows() {
        viewModel.registrationState.collectResource(
            onSuccess = {
                showLoading(false)
                showSnackbar(getString(R.string.registrazione_effettuata_con_successo))
                navigateToLoginFragment()
            },
            onError = {
                showLoading(false)
                showSnackbar(it ?: getString(R.string.errore_durante_la_registrazione))
            },
            onLoading = { showLoading(true) }
        )
    }

    override fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressCircular.visibility = if (isLoading) View.VISIBLE else View.GONE
            buttonRegister.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
            buttonRegister.isEnabled = !isLoading
        }
    }

    private fun onButtonRegisterClick() {
        val currentData = viewModel.registrationData.value
        viewModel.createUser(currentData.email, currentData.password, currentData.toUser())
    }

    private fun navigateToLoginFragment() {
        findNavController().navigate(RegisterFragment5Directions.actionRegisterFragment5ToLoginFragment())
    }
}