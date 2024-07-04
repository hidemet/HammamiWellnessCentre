package com.example.hammami.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.hammami.R
import com.example.hammami.database.FirebaseDb
import com.example.hammami.viewmodel.HammamiViewModel
import com.example.hammami.viewmodel.ViewModelFactory
import dagger.hilt.android.AndroidEntryPoint

class LoginRegisterActivity : AppCompatActivity() {
    val viewModel by lazy {
        val firebaseDb = FirebaseDb()
        val viewModelFactory =  ViewModelFactory(firebaseDb)
        ViewModelProvider(this,viewModelFactory)[HammamiViewModel::class.java]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

    }
}