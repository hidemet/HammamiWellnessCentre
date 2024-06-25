package com.example.hammami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hammami.database.FirebaseDb

class ViewModelFactory(
    private val firebaseDatabase : FirebaseDb
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HammamiViewModel::class.java)) {
            return HammamiViewModel(firebaseDatabase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}