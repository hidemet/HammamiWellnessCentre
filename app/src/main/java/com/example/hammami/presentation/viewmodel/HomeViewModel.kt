package com.example.hammami.presentation.viewmodel

import androidx.lifecycle.ViewModel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.hammami.domain.usecase.auth.CheckAuthStateUseCase


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val checkAuthStateUseCase: CheckAuthStateUseCase
) : ViewModel() {
    fun isUserAuthenticated(): Boolean {
        return checkAuthStateUseCase()
    }
}