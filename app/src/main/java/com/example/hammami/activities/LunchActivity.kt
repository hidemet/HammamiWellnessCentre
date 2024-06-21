package com.example.hammami.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.hammami.R
import com.example.hammami.database.FirebaseDb
import com.example.hammami.viewmodel.ViewModelFactory

class LunchActivity : AppCompatActivity() {

    private val firebaseDb by lazy { FirebaseDb() }
    private val viewModelFactory by lazy { ViewModelFactory(firebaseDb) }

    val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[HammamiViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lunch)
        supportActionBar?.hide()
    }
}

