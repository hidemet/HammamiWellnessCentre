package com.example.hammami.fragments.loginResigter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hammami.R
import com.example.hammami.activities.LoginRegisterActivity
import com.example.hammami.databinding.FragmentRegister3Binding
import com.example.hammami.viewmodel.HammamiViewModel

private val TAG = "RegisterFragment3"
class RegisterFragment3 : Fragment() {
    // FragmentRegisterBinding variabile usata per manipolare gli elementi dell'interfaccia utente
    // definiti nel relativo layout xml associato a RegisterFragment
    private lateinit var binding: FragmentRegister3Binding
    lateinit var viewModel: HammamiViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (activity as LoginRegisterActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // iniizializiamo il collegamento il modo che il binding sia uguale a FragmentRegisterBinding
        binding = FragmentRegister3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    /*
    * Osserva il flusso di dati emesso da RegisterViewModel e reagisce di conseguenza
    * lifecycleScope.launchWhenStarted{} è un blocco di codice che viene eseguito quando il LifeCycle
    * del fragment è nello stato STARTED (viene avviato). E' una coroutine. Le corotine sono utilizzate
    * per esequire operazioni asincrone in Kotlin.
    */


}