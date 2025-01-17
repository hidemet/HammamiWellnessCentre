package com.example.hammami.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.result.Result
import com.example.hammami.domain.usecase.user.GetUserRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InitialViewModel @Inject constructor(
    private val getUserRoleUseCase: GetUserRoleUseCase
) : ViewModel() {

    private val _userRole = MutableStateFlow<Result<String, com.example.hammami.domain.error.DataError>?>(null)
    val userRole = _userRole.asStateFlow()

    init {
        loadUserRole()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            getUserRoleUseCase().collect { result ->
                _userRole.value = result
            }
        }
    }
}