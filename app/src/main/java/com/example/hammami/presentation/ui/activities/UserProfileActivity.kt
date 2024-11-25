package com.example.hammami.presentation.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.hammami.R
import com.example.hammami.databinding.ActivityUserProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.integrity.internal.ac
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private val viewModel: UserProfileViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeEvents()

        // Carica i dati dell'utente all'avvio
        viewModel.onEvent(UserProfileViewModel.UserProfileEvent.LoadUserData)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.profile_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            viewModel.events.collect { event ->
                Log.d("UserProfileActivity", "Received event: $event")
                when (event) {
                    is UserProfileViewModel.UiEvent.SignOut -> {
                        Log.d("UserProfileActivity", "Processing SignOut event")
                        // Aggiungi questi flag per assicurarti che l'activity venga rimossa dallo stack
                        val intent = Intent(this@UserProfileActivity, InitialActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                        finish()
                    }
                    is UserProfileViewModel.UiEvent.ShowSnackbar -> {
                        Log.d("UserProfileActivity", "Showing snackbar: ${event.message}")
                        Snackbar.make(
                            binding.root,
                            event.message.asString(this@UserProfileActivity),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    UserProfileViewModel.UiEvent.AccountDeleted -> TODO()
                }
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, UserProfileActivity::class.java))
        }
    }
}