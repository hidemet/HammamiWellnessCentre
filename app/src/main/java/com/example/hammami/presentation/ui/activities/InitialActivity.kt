package com.example.hammami.presentation.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hammami.core.result.Result

import com.example.hammami.databinding.ActivityInitialBinding
import com.example.hammami.presentation.viewmodel.InitialViewModel
import com.example.hammami.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class InitialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInitialBinding
    @Inject
    lateinit var preferencesManager: PreferencesManager
    private var keepSplashScreenOn = true

    private val viewModel: InitialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition { keepSplashScreenOn }
        binding = ActivityInitialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Reindirizza dopo un breve ritardo (per mostrare la splash screen)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Controlla se l'utente è loggato
                if (preferencesManager.isUserLoggedIn()) {
                    Log.d("InitialActivity", "Utente è loggato")
                    // L'utente è loggato, osserva il ruolo
                    launch {
                        viewModel.userRole.collect { roleResult ->
                            keepSplashScreenOn = false
                            when (roleResult) {
                                is Result.Success -> {
                                    Log.d("InitialActivity", "Ruolo utente: ${roleResult.data}")
                                    if (roleResult.data == "admin") {
                                        navigateToAdmin()
                                    } else {
                                        navigateToMain()
                                    }
                                }
                                is Result.Error -> {
                                    // Gestisci l'errore, ad esempio mostrando un messaggio o tornando alla schermata di login
                                    Log.d("InitialActivity", "Errore ${roleResult.error}")
                                    navigateToLogin()
                                }
                                else -> {}
                            }
                        }
                    }
                } else {
                    // L'utente non è loggato, vai alla schermata di login dopo un breve ritardo
                    Log.d("InitialActivity", "Utente non è loggato")
                    keepSplashScreenOn = false
                    navigateToLogin()
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginRegisterActivity::class.java))
        finish()
    }

    private fun navigateToAdmin() {
        startActivity(Intent(this, AdminActivity::class.java))
        finish()
    }

}