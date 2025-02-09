package com.example.hammami.presentation.ui.activities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.hammami.domain.usecase.user.ObserveUserChangesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class InitialViewModel @Inject constructor(
    private val observeUserChangesUseCase: ObserveUserChangesUseCase,
    private val userRepository: UserRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            observeUserChangesUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val user = result.data
                        if (user != null) {
                            Log.d("InitialViewModel", "observeUser: User is not null, getting ID")

                            // Ottieni l'ID utente *prima* di entrare nel ciclo di isAdmin
                            val userIdResult = getCurrentUserIdUseCase()
                            if (userIdResult is Result.Error) {
                                Log.e("InitialViewModel", "observeUser: Error getting user ID: ${userIdResult.error}")
                                _navigationEvent.emit(NavigationEvent.NavigateToLogin) // Gestisci l'errore
                                return@collect // Esci dalla collect se non hai l'ID
                            }
                            val userId = (userIdResult as Result.Success).data
                            Log.d("InitialViewModel", "observeUser: User ID: $userId")

                            // Avvia la verifica di isAdmin *PRIMA* di collectLatest
                            userRepository.isUserAdmin(userId)

                            var attempts = 0
                            val maxAttempts = 3
                            val delayDuration = 1000L

                            // Osserva lo StateFlow *dentro* un ciclo while, con timeout.
                            while (attempts < maxAttempts) {
                                attempts++ // Incrementa *all'inizio* del ciclo
                                Log.d("InitialViewModel", "observeUser: Attempt #$attempts to get isAdmin")

                                val isAdmin = withTimeoutOrNull(5000L) {
                                    userRepository.isAdmin.first { it != null } // Aspetta il *primo* valore non-null
                                }

                                if (isAdmin != null) {
                                    Log.d("InitialViewModel", "observeUser: isAdmin value: $isAdmin")
                                    if (isAdmin) {
                                        _navigationEvent.emit(NavigationEvent.NavigateToAdmin)
                                    } else {
                                        _navigationEvent.emit(NavigationEvent.NavigateToMain)
                                    }
                                    return@collect // Esci dalla collect *e* dal ciclo
                                } else {
                                    // Timeout!
                                    // RIMUOVI QUESTO CONTROLLO: if (attempts < maxAttempts) {
                                    if(attempts < maxAttempts){
                                        Log.d("InitialViewModel", "isAdmin is still null, retrying in $delayDuration ms")
                                        delay(delayDuration)
                                    } else { // Aggiungi questo else
                                        Log.d("InitialViewModel", "isAdmin is null after $maxAttempts attempts, navigating to login")
                                        _navigationEvent.emit(NavigationEvent.NavigateToLogin) // Timeout!
                                        return@collect // Esci dalla collect in caso di timeout
                                    }
                                }
                            }


                        } else {
                            Log.d("InitialViewModel", "observeUser: User is null, navigating to login")
                            _navigationEvent.emit(NavigationEvent.NavigateToLogin)
                        }
                    }
                    is Result.Error -> {
                        Log.e("InitialViewModel", "observeUser: Error: ${result.error}")
                        _navigationEvent.emit(NavigationEvent.NavigateToLogin)
                    }
                }
            }
        }
    }



    sealed class NavigationEvent {
        object NavigateToMain : NavigationEvent()
        object NavigateToAdmin : NavigationEvent()
        object NavigateToLogin : NavigationEvent()
    }
}