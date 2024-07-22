package com.example.hammami.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.hammami.R
import com.example.hammami.viewmodel.InitialViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.hammami.util.Resource
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class InitialActivity : AppCompatActivity() {
    private val viewModel: InitialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }

        setContentView(R.layout.activity_initial)

        lifecycleScope.launch {
            viewModel.authState.collectLatest { state ->
                when (state) {
                    is Resource.Success -> navigateToMain()
                    is Resource.Error -> navigateToLogin()
                    is Resource.Loading -> {} // Mantieni la splash screen
                    is Resource.Unspecified -> {} // Gestisci questo stato se necessario
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
}