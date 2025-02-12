package com.example.hammami.presentation.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hammami.R
import com.example.hammami.databinding.ActivityInitialBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InitialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInitialBinding
    private val viewModel: InitialViewModel by viewModels()
    private var keepSplashScreenOn = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        continueToNextActivity(isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashScreenOn }
        binding = ActivityInitialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Controlla e richiedi i permessi prima di controllare l'autenticazione
        checkPermissionsAndContinue()
    }

    private fun checkPermissionsAndContinue() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            continueToNextActivity(true)
        } else {
            // Richiedi il permesso.  Il risultato verrà gestito in requestPermissionLauncher.
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showPermissionRationale() {
        // Da una spiegazione sulle ragioni per cui il permesso è richiesto il permesso
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.permission_required_title))
            .setMessage(getString(R.string.notification_permission_rationale))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                // Richiedi di nuovo il permesso.  L'utente avrà un'altra
                // possibilità di concederlo.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            //L'utente può scegliere se continuare o no
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }


    private fun continueToNextActivity(permissionGranted: Boolean) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    keepSplashScreenOn = false
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
        if(!permissionGranted) {
            showPermissionRationale()
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