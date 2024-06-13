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
import com.example.hammami.data.User
import com.example.hammami.databinding.FragmentRegisterBinding
import com.example.hammami.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint


private val TAG = "RegisterFragment"
@AndroidEntryPoint
class RegisterFragment : Fragment (R.layout.fragment_register){

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        lifecycleScope.launchWhenStarted{
            viewModel.register.collect { event ->
                when(event){
                    is Resource.Loading -> {
                     binding.buttonRegisterResgister.startAnimation()
                    }
                    is Resource.Success -> {
                        Log.d("test",event.message.toString())
                        binding.buttonRegisterResgister.revertAnimation()
                    }

                    is Resource.Error -> {
                        Log.e(TAG, event.message,toString())
                        binding.buttonRegisterResgister.revertAnimation()
                }
            }
        }
    }

}