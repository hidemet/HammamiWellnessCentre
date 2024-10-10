package com.example.hammami.presentation.viewmodel

import androidx.lifecycle.ViewModel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.hammami.data.repositories.UserRepository


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    // Logica specifica per la Home...
}