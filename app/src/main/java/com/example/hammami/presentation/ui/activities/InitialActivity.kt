package com.example.hammami.presentation.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.ActivityInitialBinding
import com.example.hammami.presentation.viewmodel.InitialViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InitialActivity : AppCompatActivity() {

    private val viewModel: InitialViewModel by viewModels()
    private lateinit var binding: ActivityInitialBinding
    private var keepSplashScreenOn = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSplashScreen()
        setupBinding()
        observeUiState()
    }

    private fun setupSplashScreen() {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashScreenOn }
    }

    private fun setupBinding() {
        binding = ActivityInitialBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun observeUiState() = lifecycleScope.launch {
        viewModel.uiState.collect { state ->
            handleState(state)
        }
    }

    private fun handleState(state: InitialViewModel.UiState) {
        when (state) {
            is InitialViewModel.UiState.Loading -> showLoading()
            is InitialViewModel.UiState.LoggedIn -> navigateToMain()
            is InitialViewModel.UiState.NotLoggedIn -> navigateToLogin()
            is InitialViewModel.UiState.Error -> handleError(state.message)
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
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

    private fun handleError(message: UiText) {
        keepSplashScreenOn = false
        binding.progressBar.isVisible = false
        Toast.makeText(this, message.asString(this), Toast.LENGTH_LONG).show()
        navigateToLogin()
    }
}