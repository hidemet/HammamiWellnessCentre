package com.example.hammami.presentation.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.hammami.R
import com.example.hammami.presentation.ui.fragments.loginResigter.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginRegisterActivity : AppCompatActivity() {
    private val viewModel: RegisterViewModel by viewModels()

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, LoginRegisterActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

    }
}