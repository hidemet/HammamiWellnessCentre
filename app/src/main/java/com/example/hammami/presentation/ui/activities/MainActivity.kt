package com.example.hammami.presentation.ui.activities

import android.content.Context
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
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        setupNavigation()
    }

    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.profileFragment,
                R.id.editUserProfileFragment,
                R.id.giftCardsFragment,
                R.id.availableGiftCardsFragment,
                R.id.giftCardSummaryFragment,
                R.id.editContactInfoFragment,
                R.id.editPersonalInfoFragment,
                R.id.paymentFragment,
                R.id.servizioDetailFragment,
                R.id.bookingFragment,
                R.id.bookingSummaryFragment,
                R.id.couponFragment,
                R.id.couponSelectionFragment,
                R.id.couponSuccessFragment
                -> {
                    binding.bottomNavigation.visibility = View.GONE
                }

                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        fun getStartIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java)
    }
}