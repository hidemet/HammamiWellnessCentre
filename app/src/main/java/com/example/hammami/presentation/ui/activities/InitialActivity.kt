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

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityInitialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mantieni lo splash screen visibile fino a quando non siamo pronti a mostrare l'UI
        splashScreen.setKeepOnScreenCondition { true }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is InitialViewModel.UiState.Loading -> showLoading()
                    is InitialViewModel.UiState.LoggedIn -> navigateToMainActivity()
                    is InitialViewModel.UiState.NotLoggedIn -> navigateToLoginActivity()
                    is InitialViewModel.UiState.Error -> showError(state.message)
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToLoginActivity() {
        startActivity(Intent(this, LoginRegisterActivity::class.java))
        finish()
    }

    private fun showError(message: UiText) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, message.asString(this), Toast.LENGTH_LONG).show()
        navigateToLoginActivity()
    }
}