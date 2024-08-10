package com.example.hammami.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.database.UserProfileRepository
import com.example.hammami.models.User
import com.example.hammami.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class EditUserProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

//    private val _currentUser = MutableStateFlow<User?>(null)
//    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _passwordChangeEvent = MutableSharedFlow<PasswordChangeResult>()
    val passwordChangeEvent: SharedFlow<PasswordChangeResult> = _passwordChangeEvent.asSharedFlow()

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
                    userProfileRepository.updateUserProfile(updatedUser, currentPassword)
                } else {
                    userProfileRepository.updateUserProfile(updatedUser)
                }
                when (result) {
                    is Resource.Success -> {
                        _profileUpdateEvent.emit(ProfileUpdateResult.Success("Profilo aggiornato con successo"))
                    }

                    is Resource.Error -> {
                        _profileUpdateEvent.emit(
                            ProfileUpdateResult.Error(
                                result.message ?: "Errore nell'aggiornamento del profilo"
                            )
                        )
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _profileUpdateEvent.emit(ProfileUpdateResult.Error("Aggiornamento fallito: ${e.message}"))
            }
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            val result = userProfileRepository.uploadProfileImage(imageUri)
            if (result is Resource.Success) {
                _profileUpdateEvent.emit(ProfileUpdateResult.Success("Image uploaded successfully"))
            } else if (result is Resource.Error) {
                _profileUpdateEvent.emit(
                    ProfileUpdateResult.Error(
                        result.message ?: "Error uploading image"
                    )
                )
            }
        }
    }

        fun updateUserProfileImage(imageUrl: String, user: User) {
            viewModelScope.launch {
                val result = userProfileRepository.updateUserProfileImage(imageUrl, user)
                if (result is Resource.Success) {
                    _profileUpdateEvent.emit(ProfileUpdateResult.Success("Profile updated successfully"))
                } else if (result is Resource.Error) {
                    _profileUpdateEvent.emit(
                        ProfileUpdateResult.Error(
                            result.message ?: "Error updating profile"
                        )
                    )
                }
            }
        }

        fun createImageUri(context: Context): Uri {
            val imageFile = File.createTempFile(
                "JPEG_${System.currentTimeMillis()}_",
                ".jpg",
                context.externalCacheDir
            )
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
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


//    fun changePassword(currentPassword: String, newPassword: String) {
//        viewModelScope.launch {
//            try {
//                val result = userProfileRepository.changePassword(currentPassword, newPassword)
//                if (result is Resource.Success) {
//                    _passwordChangeEvent.emit(PasswordChangeResult.Success("Password changed successfully"))
//                } else if (result is Resource.Error) {
//                    _passwordChangeEvent.emit(PasswordChangeResult.Error(result.message ?: "Error changing password"))
//                }
//            } catch (e: Exception) {
//                _passwordChangeEvent.emit(PasswordChangeResult.Error("Failed to change password: ${e.message}"))
//            }
//        }
//    }

fun resetPassword(email: String) {
    viewModelScope.launch {
        try {
            val result = userProfileRepository.resetPassword(email)
            if (result is Resource.Success) {
                _passwordChangeEvent.emit(PasswordChangeResult.Success("Email inviata con successo"))
            } else if (result is Resource.Error) {
                _passwordChangeEvent.emit(PasswordChangeResult.Error(result.message ?: "Errore durante l'invio dell'email"))
            }
        } catch (e: Exception) {
            _passwordChangeEvent.emit(PasswordChangeResult.Error("Errore durante l'invio dell'email: ${e.message}"))
        }
    }
}

    sealed class PasswordChangeResult {
        data class Success(val message: String) : PasswordChangeResult()
        data class Error(val message: String) : PasswordChangeResult()
    }


    sealed class ProfileUpdateResult {
        data class Success(val message: String) : ProfileUpdateResult()
        data class Error(val message: String) : ProfileUpdateResult()
    }
}