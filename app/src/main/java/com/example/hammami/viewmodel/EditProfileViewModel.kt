package com.example.hammami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.models.User
import com.example.hammami.models.UserRepository
import com.example.hammami.util.RegisterFieldsState
import com.example.hammami.util.RegisterValidation
import com.example.hammami.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val userState: StateFlow<Resource<User>> = _userState.asStateFlow()

    private val _validationState = MutableStateFlow<RegisterFieldsState?>(null)
    val validationState: StateFlow<RegisterFieldsState?> = _validationState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.authState.collectLatest { resource ->
                _userState.value = resource
            }
        }
    }

    fun validateAndUpdateUser(updatedUser: User) {
        val validationResult = validateUserInput(updatedUser)
        _validationState.value = validationResult

        if (validationResult.isValid()) {
            viewModelScope.launch {
                // Implement the update logic in UserRepository
                userRepository.updateUserData(updatedUser)
            }
        }
    }

    private fun validateUserInput(user: User): RegisterFieldsState {
        // Implement validation logic here
        // Return RegisterFieldsState with appropriate validation results
        TODO("Implement validation logic")
    }
}

// Extension function to check if all fields are valid
fun RegisterFieldsState.isValid(): Boolean {
    return firstName is RegisterValidation.Success &&
            lastName is RegisterValidation.Success &&
            email is RegisterValidation.Success &&
            birthDate is RegisterValidation.Success &&
            gender is RegisterValidation.Success
}