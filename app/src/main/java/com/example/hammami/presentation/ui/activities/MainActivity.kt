package com.example.hammami.presentation.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: UserProfileViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeUserState()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun observeUserState() {
        lifecycleScope.launch {
            viewModel.userState.collect { state ->
                when (state) {
                    is UserProfileViewModel.UserState.Loading -> showLoading(true)
                    is UserProfileViewModel.UserState.LoggedIn -> {
                        showLoading(false)
                        // L'utente è loggato, puoi aggiornare l'UI di conseguenza
                    }
                    is UserProfileViewModel.UserState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    is UserProfileViewModel.UserState.NotLoggedIn -> {
                        navigateToLogin()
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: UiText) {
        Toast.makeText(this, message.asString(this), Toast.LENGTH_LONG).show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginRegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}