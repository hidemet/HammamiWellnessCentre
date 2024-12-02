package com.example.hammami.presentation.ui.features.service

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.Service
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.domain.usecase.GetEpilazioneUseCase
import com.example.hammami.domain.usecase.GetTrattCorpoUseCase
import com.example.hammami.domain.usecase.GetTrattVisoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EsteticaViewModel @Inject constructor(
    private val getEpilazioneUseCase: GetEpilazioneUseCase,
    private val getTrattCorpoUseCase: GetTrattCorpoUseCase,
    private val getTrattVisoUseCase: GetTrattVisoUseCase
): ViewModel() {

    private val _allEpilazione = MutableStateFlow(EsteticaServices())
    val allEpilazione = _allEpilazione.asStateFlow()

    private val _allTrattCorpo = MutableStateFlow(EsteticaServices())
    val allTrattCorpo = _allTrattCorpo.asStateFlow()

    private val _allTrattViso = MutableStateFlow(EsteticaServices())
    val allTrattViso = _allTrattViso.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            loadEpilazioneData()
            loadTrattCorpoData()
            loadTrattVisoData()
        }
    }

    private suspend fun loadEpilazioneData(){
        when (val result = getEpilazioneUseCase()) {
            is Result.Success -> {
                _allEpilazione.update { it.copy(servicesEpilazione = result.data) }
            }
            is Result.Error -> {
                Log.e("EsteticaViewModel", "Errore in loadEpilazioneData")
                emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    private suspend fun loadTrattVisoData(){
        when (val result = getTrattVisoUseCase()) {
            is Result.Success -> {
                _allTrattViso.update { it.copy(servicesTrattViso = result.data) }
            }
            is Result.Error -> {
                Log.e("EsteticaViewModel", "Errore in loadTrattVisoData")
                emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    private suspend fun loadTrattCorpoData(){
        when (val result = getTrattCorpoUseCase()) {
            is Result.Success -> {
                _allTrattCorpo.update { it.copy(servicesTrattCorpo = result.data) }
            }
            is Result.Error -> {
                Log.e("EsteticaViewModel", "Errore in loadTrattCorpoData")
                emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    data class EsteticaServices(
        val servicesEpilazione: List<Service> = emptyList(),
        val servicesTrattCorpo: List<Service> = emptyList(),
        val servicesTrattViso: List<Service> = emptyList()
    )

    /*
    data class TrattCorpoServices(
        val servicesTrattCorpo: List<Service> = emptyList()
    )

    data class TrattVisoServices(
        val servicesTrattViso: List<Service> = emptyList()
    )

     */

    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    sealed class UiEvent {
        data class ShowError(val message: UiText) : UiEvent()
        data class ShowMessage(val message: UiText) : UiEvent()
    }
}