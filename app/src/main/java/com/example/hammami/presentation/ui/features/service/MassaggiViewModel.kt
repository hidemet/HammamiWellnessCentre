package com.example.hammami.presentation.ui.features.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.Service
import com.example.hammami.domain.usecase.GetMassaggiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MassaggiViewModel @Inject constructor(
    private val getMassaggiUseCase: GetMassaggiUseCase
): ViewModel() {

    private val _allMassaggi = MutableStateFlow(MassaggiServices())
    val allMassaggi = _allMassaggi.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            loadMassaggiData()
        }
    }

    private suspend fun loadMassaggiData(){
        when (val result = getMassaggiUseCase()) {
            is Result.Success -> {
                _allMassaggi.update { it.copy(servicesMassaggi = result.data) }
            }
            is Result.Error -> {
                emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    data class MassaggiServices(
        val servicesMassaggi: List<Service> = emptyList()
    )

    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    sealed class UiEvent {
        data class ShowError(val message: UiText) : UiEvent()
        data class ShowMessage(val message: UiText) : UiEvent()
    }

    /*
    fun fetchAllMassaggi() {
        viewModelScope.launch {
            val allServices = mutableListOf<Service>()

            try {
                val massaggiSnapshot = firestore.collection("/Servizi/Massaggi/trattamenti").get().await()
                allServices.addAll(massaggiSnapshot.toObjects(Service::class.java))

                _allMassaggi.emit(Resource.Success(allServices))
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel recupero dei nuovi servizi: ${e.message}", e)
                _allMassaggi.emit(
                    Resource.Error(
                        e.message ?: "Si è verificato un errore sconosciuto"
                    )
                )
            }
        }
    }
     */
}