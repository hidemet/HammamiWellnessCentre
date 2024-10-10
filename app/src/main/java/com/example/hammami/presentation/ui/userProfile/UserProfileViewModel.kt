package com.example.hammami.presentation.ui.userProfile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.domain.usecase.DeleteUserUseCase
import com.example.hammami.domain.usecase.GetUserProfileUseCase
import com.example.hammami.domain.usecase.SignOutUseCase
import com.example.hammami.domain.usecase.UpdateUserProfileUseCase
import com.example.hammami.domain.usecase.UploadUserImageUseCase
import com.example.hammami.domain.usecase.Result
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadUserImageUseCase: UploadUserImageUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UserProfileState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        object Idle : UiEvent()
        object Loading : UiEvent()
        data class ShowError(val error: UiText) : UiEvent()
        data class ProfileUpdateSuccess(val message: UiText) : UiEvent()
        object SignOutSuccess : UiEvent()
        object UserDeleted : UiEvent()
    }

    init {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.Loading)
            getUserProfileUseCase().collect { result ->
                when (result) {
                    is Result.Success -> updateState { copy(user = result.data) }
                    is Result.Error -> _uiEvent.emit(UiEvent.ShowError(result.error.asUiText()))
                }
                _uiEvent.emit(UiEvent.Idle)
            }
        }
    }

    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.Loading)
            when (val result = updateUserProfileUseCase(user)) {
                is Result.Success -> {
                    updateState { copy(user = user) } // Aggiorniamo lo stato con l'utente passato come parametro
                    _uiEvent.emit(UiEvent.ProfileUpdateSuccess(UiText.StringResource(R.string.profile_updated_successfully)))
                }
                is Result.Error -> _uiEvent.emit(UiEvent.ShowError(result.error.asUiText()))
            }
            _uiEvent.emit(UiEvent.Idle)
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.Loading)
            when (val result = uploadUserImageUseCase(imageUri)) {
                is Result.Success -> {
                    state.value.user?.let { currentUser ->
                        updateUserProfile(currentUser.copy(profileImage = result.data))
                    }
                }
                is Result.Error -> _uiEvent.emit(UiEvent.ShowError(result.error.asUiText()))
            }
            _uiEvent.emit(UiEvent.Idle)
        }
    }

    fun deleteUserProfile() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.Loading)
            when (val result = deleteUserUseCase()) {
                is Result.Success -> {
                    updateState { copy(user = null) }
                    _uiEvent.emit(UiEvent.UserDeleted)
                }
                is Result.Error -> _uiEvent.emit(UiEvent.ShowError(result.error.asUiText()))
            }
            _uiEvent.emit(UiEvent.Idle)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.Loading)
            when (val result = signOutUseCase()) {
                is Result.Success -> _uiEvent.emit(UiEvent.SignOutSuccess)
                is Result.Error -> _uiEvent.emit(UiEvent.ShowError(result.error.asUiText()))
            }
            _uiEvent.emit(UiEvent.Idle)
        }
    }

    private fun updateState(update: UserProfileState.() -> UserProfileState) {
        _state.update(update)
    }

    data class UserProfileState(
        val user: User? = null
    )
}