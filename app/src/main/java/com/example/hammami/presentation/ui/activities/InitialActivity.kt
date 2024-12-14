package com.example.hammami.presentation.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import com.example.hammami.databinding.ActivityInitialBinding
import com.example.hammami.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InitialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInitialBinding
    @Inject
    lateinit var preferencesManager: PreferencesManager
    private var keepSplashScreenOn = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSplashScreen()
        setupBinding()

        // Reindirizza dopo un breve ritardo (per mostrare la splash screen)
        Handler(Looper.getMainLooper()).postDelayed({
            if (preferencesManager.isUserLoggedIn()) {
                navigateToMain()
            } else {
                navigateToLogin()
            }
        }, 1000) // Ritardo di 1 secondo (modifica se necessario)

    }

    private fun setupSplashScreen() {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashScreenOn }
    }

    private fun setupBinding() {
        binding = ActivityInitialBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun navigateToMain() {
        keepSplashScreenOn = false
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToLogin() {
        keepSplashScreenOn = false
        startActivity(Intent(this, LoginRegisterActivity::class.java))
        finish()
    }

}
