package com.example.hammami.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.database.UserProfileRepository
import com.example.hammami.models.User
import com.example.hammami.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class EditUserProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

//    private val _currentUser = MutableStateFlow<User?>(null)
//    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()



    // Nuovo SharedFlow per notificare gli aggiornamenti del profilo
    private val _profileUpdateEvent = MutableSharedFlow<ProfileUpdateResult>()
    val profileUpdateEvent: SharedFlow<ProfileUpdateResult> = _profileUpdateEvent.asSharedFlow()

    val user: StateFlow<Resource<User>> = userProfileRepository.authState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Loading()
    )

//    fun refreshUser() {
//        viewModelScope.launch {
//            userProfileRepository.refreshUser()
//        }
//    }

//    init {
//        fetchUserProfile()
//    }


    fun updateUserProfile(updatedUser: User, context: Context, currentPassword: String? = null) {
        viewModelScope.launch {
            try {
                val result = if (currentPassword != null) {
                    userProfileRepository.updateUserProfile(updatedUser, context, currentPassword)
                } else {
                    userProfileRepository.updateUserProfile(updatedUser, context)
                }
                when (result) {
                    is Resource.Success -> {
                        _profileUpdateEvent.emit(ProfileUpdateResult.Success("Profilo aggiornato con successo"))
                    }
                    is Resource.Error -> {
                        _profileUpdateEvent.emit(ProfileUpdateResult.Error(result.message ?: "Errore nell'aggiornamento del profilo"))
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _profileUpdateEvent.emit(ProfileUpdateResult.Error("Aggiornamento fallito: ${e.message}"))
            }
        }
    }
//    fun fetchUserProfile() {
//        viewModelScope.launch {
//            Log.d("EditUserProfile", "Fetching user profile")
//            _userProfileState.value = Resource.Loading()
//            _userProfileState.value = userProfileRepository.fetchCurrentUserProfile()
//            Log.d("EditUserProfile", "Fetched user profile: ${_userProfileState.value}")
//        }
//    }

//    fun updateUserProfile(updatedUser: User, context: Context, currentPassword: String) {
//        viewModelScope.launch {
//            val result = userProfileRepository.updateUserProfile(updatedUser, context, currentPassword)
//            handleUpdateResult(result, updatedUser)
//        }
//
//    }

//    fun fetchUserProfile() {
//        viewModelScope.launch {
//            when (val result = userProfileRepository.fetchCurrentUserProfile()) {
//                is Resource.Success -> {
//                    Log.d("EditUserProfile", "Fetched user profile: ${result.data}")
//                    _currentUser.value = result.data
//                }
//                is Resource.Error -> {
//                    Log.e("EditUserProfile", "Error fetching user profile: ${result.message}")
//                }
//                else -> {}
//            }
//        }
//    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            userProfileRepository.refreshUser()
        }
    }
//    private suspend fun handleUpdateResult(result: Resource<User>, updatedUser: User) {
//        when (result) {
//            is Resource.Success -> {
//                if (updatedUser.email != result.data?.email) {
//                    _profileUpdateEvent.emit(ProfileUpdateResult.Success("Profilo aggiornato con successo. Controlla la tua email per confermare la modifica."))
//                } else {
//                    _profileUpdateEvent.emit(ProfileUpdateResult.Success("Profilo aggiornato con successo."))
//                }
//            }
//
//            is Resource.Error -> _profileUpdateEvent.emit(
//                ProfileUpdateResult.Error(
//                    result.message ?: "An unknown error occurred"
//                )
//            )
//
//            else -> {} // Handle other cases if necessary
//        }
//    }

    sealed class ProfileUpdateResult {
        data class Success(val message: String) : ProfileUpdateResult()
        data class Error(val message: String) : ProfileUpdateResult()
    }
}