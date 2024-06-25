package com.example.hammami.fragments.loginResigter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hammami.R
import com.example.hammami.activities.LoginRegisterActivity
import com.example.hammami.activities.ShoppingActivity
import com.example.hammami.databinding.FragmentLoginBinding
import com.example.hammami.viewmodel.HammamiViewModel



class LoginFragment : Fragment() {

    val TAG: String = "LoginFragment"

    private lateinit var binding: FragmentLoginBinding
    private lateinit var buttonLogin: Button
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
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonLogin = view.findViewById(R.id.button_login)

        onLoginClick()
        observerLogin()
        observerLoginError()


        //----------------------------------------------
  /*
        binding.apply {
            button_login.setOnClickListener {
                val email = edEmailLogin.text.toString().trim()
                val password = edPasswordLogin.text.toString()
                viewModel.login(email, password)
            }
        }
*/
/*
        lifecycleScope.launchWhenStarted {
            viewModel.login.collect {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressBarLogin.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressBarLogin.visibility = View.GONE
                        Toast.makeText(requireContext(), "Login Success", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Error -> {
                        binding.progressBarLogin.visibility = View.GONE
                        Toast.makeText(requireContext(), "Login Failed", Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

 */
    }

    private fun onLoginClick() {
        buttonLogin.setOnClickListener {
            val email = getEmail()?.trim()
            val password = getPassword()
            if (email != null && password != null) {
                viewModel.loginUser(email, password)
            }

        }
    }

    private fun getPassword(): String? {
        val password = binding.textFieldPassword.editText?.text.toString()
        var error: String? = null
        when {
            password.isBlank() -> error = getString(R.string.err_password_obbligatoria)
        }
        binding.textFieldPassword.error = error
        if (error != null) {
            binding.textFieldPassword.requestFocus()
        }
        return password.takeIf { error == null }
    }

    private fun getEmail(): String? {
        val email = binding.textFieldEmail.editText?.text.toString()
        var error: String? = null
        when {
            email.isBlank() -> error = getString(R.string.err_email_obbligatoria)
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> error = getString(R.string.err_email_non_valida)
        }
        binding.textFieldEmail.error = error
        if (error != null) {
            binding.textFieldEmail.requestFocus()
        }
        return email.takeIf { error == null }
    }

    private fun observerLoginError() {
        viewModel.loginError.observe(viewLifecycleOwner) { error ->
            Log.e(TAG, error)
            Toast.makeText(activity, "Controlla le informazioni inserite", Toast.LENGTH_LONG).show()
        }
    }


        /**
         * I metodi observerLoginError e observerLogin osservano i cambiamenti nei dati di loginError e login
         * esposti dal ViewModel associato al Fragment
         */

    private fun observerLogin() {
        viewModel.login.observe(viewLifecycleOwner) { isLogged ->
            if (isLogged) {
                val intent = Intent(activity, ShoppingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }
    }


