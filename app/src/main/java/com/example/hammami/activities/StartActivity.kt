package com.example.hammami.activities


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hammami.R
import com.example.hammami.databinding.ActivityInitialBinding
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "StartActivity"

@AndroidEntryPoint

class StartActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityInitialBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navController = findNavController(R.id.homeHostFragment)
        binding.bottomNavigation.setupWithNavController(navController)
    }

}