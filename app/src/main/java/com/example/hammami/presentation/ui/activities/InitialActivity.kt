package com.example.hammami.presentation.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hammami.databinding.ActivityInitialBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InitialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInitialBinding
    private val viewModel: InitialViewModel by viewModels()
    private var keepSplashScreenOn = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashScreenOn }
        binding = ActivityInitialBinding.inflate(layoutInflater)
        setContentView(binding.root)


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    when (event) {
                        InitialViewModel.NavigationEvent.NavigateToMain -> {
                            navigateToMain()
                        }
                        InitialViewModel.NavigationEvent.NavigateToAdmin -> {
                            navigateToAdmin()
                        }
                        InitialViewModel.NavigationEvent.NavigateToLogin -> {
                            navigateToLogin()
                        }
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToAdmin() {
        startActivity(Intent(this, AdminActivity::class.java))
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginRegisterActivity::class.java))
        finish()
    }
}