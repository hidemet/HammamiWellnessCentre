package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.hammami.R
import com.example.hammami.databinding.FragmentRegisterBinding
import com.example.hammami.model.User
import com.example.hammami.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment1 : Fragment(R.layout.fragment_register1) {
    // FragmentRegisterBinding variabile usata per manipolare gli elementi dell'interfaccia utente
    // definiti nel relativo layout xml associato a RegisterFragment
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // iniizializiamo il collegamento il modo che il binding sia uguale a FragmentRegisterBinding
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            buttonRegisterRegister.setOnClickListener {
                val user = User(
                    edFirstNameRegister.text.toString().trim(),
                    edLastNameRegister.text.toString().trim(),
                    edEmailRegister.text.toString().trim()
                )

                val password = edPasswordRegister.text.toString()
                viewModel.createAccount(user, password)
            }
        }

        /*
        * Osserva il flusso di dati emesso da RegisterViewModel e reagisce di conseguenza
        * lifecycleScope.launchWhenStarted{} è un blocco di codice che viene eseguito quando il LifeCycle
        * del fragment è nello stato STARTED (viene avviato). E' una coroutine. Le corotine sono utilizzate
        * per esequire operazioni asincrone in Kotlin.
        */
        lifecycleScope.launchWhenStarted {
            viewModel.register.collect { event ->
                when (event) {
                    is Resource.Loading -> {
                        binding.buttonRegisterResgister.startAnimation()
                    }

                    is Resource.Success -> {
                        Log.d("test", event.message.toString())
                        binding.buttonRegisterResgister.revertAnimation()
                    }

                    is Resource.Error -> {
                        Log.e(TAG, event.message, toString())
                        binding.buttonRegisterResgister.revertAnimation()
                    }
                }
            }
        }

    }
}