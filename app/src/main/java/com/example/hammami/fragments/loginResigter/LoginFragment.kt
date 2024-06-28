package com.example.hammami.fragments.loginResigter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.activities.LoginRegisterActivity
import com.example.hammami.activities.HomeActivity
import com.example.hammami.databinding.FragmentLoginBinding
import com.example.hammami.viewmodel.HammamiViewModel
import com.google.android.material.textfield.TextInputLayout

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
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
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonLogin.setOnClickListener { onLoginClick() }
        binding.buttonRegister.setOnClickListener { onRegisterClick() }
        viewModel.loginError.observe(viewLifecycleOwner) { error ->
            Toast.makeText(activity, "Controlla le informazioni inserite", Toast.LENGTH_LONG).show()
        }
        viewModel.login.observe(viewLifecycleOwner) { isLogged ->
            if (isLogged) {
                val intent = Intent(activity, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    private fun onLoginClick() {
        val email = validateAndReturnField(
            binding.textFieldEmail,
            getString(R.string.err_email_obbligatoria),
            getString(R.string.err_email_non_valida)
        ) { input: String -> android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches() }
        val password =
            validateAndReturnField(binding.textFieldPassword, getString(R.string.err_password_obbligatoria))
        if (email != null && password != null) {
            viewModel.loginUser(email, password)
        }
    }

    private fun validateAndReturnField(
        field: TextInputLayout,
        emptyError: String,
        invalidError: String? = null,
        validation: ((String) -> Boolean)? = null
    ): String? {
        val text = field.editText?.text.toString()
        var error: String? = null
        when {
            text.isBlank() -> error = emptyError
            validation != null && !validation(text) -> error = invalidError
        }
        field.error = error
        return text.takeIf { error == null }
    }

    private fun onRegisterClick() {
        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment1)
        }
    }
}


