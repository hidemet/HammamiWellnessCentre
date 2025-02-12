package com.example.hammami.presentation.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.hammami.R
import com.example.hammami.databinding.ActivityAdminBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_admin) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationAdmin.setupWithNavController(navController)

        // Aggiungi un listener per gestire la visibilità della
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeAdminFragment, R.id.agendaAdminFragment, R.id.adminServiceCatalogueFragment -> {
                    // Mostra la BottomNavigationView solo per queste destinazioni
                    binding.bottomNavigationAdmin.visibility = View.VISIBLE
                }
                else -> {
                    // Nascondi la BottomNavigationView per tutte le altre destinazioni
                    binding.bottomNavigationAdmin.visibility = View.GONE
                }
            }
        }

    }
}