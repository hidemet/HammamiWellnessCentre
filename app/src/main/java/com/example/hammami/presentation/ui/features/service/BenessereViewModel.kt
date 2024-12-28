package com.example.hammami.presentation.ui.features.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.model.Service
import com.example.hammami.core.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.hammami.domain.usecase.GetBenessereUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class BenessereViewModel @Inject constructor(
    private val getBenessereUseCase: GetBenessereUseCase
): ViewModel() {

    private val _allBenessere = MutableStateFlow(BenessereServices())
    val allBenessere = _allBenessere.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        //fetchAllBenessere()
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            loadBenessereData()
        }
    }

    private suspend fun loadBenessereData(){
        when (val result = getBenessereUseCase()) {
            is Result.Success -> {
                _allBenessere.update { it.copy(servicesBenessere = result.data) }
            }
            is Result.Error -> {
                emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
            }

        }
    }

    private fun onServiceSelected(service: Service) {
        //emitUiEvent(UiEvent.NavigateToBenessereDetail(service))
        //bisogna implementare la navigazione
    }

    data class BenessereServices(
        val servicesBenessere: List<Service> = emptyList()
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
    fun fetchAllBenessere() {
        viewModelScope.launch {
            val allServices = mutableListOf<Service>()

            try {
                val benessereSnapshot = firestore.collection("/Servizi/Benessere/trattamenti").get().await()
                allServices.addAll(benessereSnapshot.toObjects(Service::class.java))

                _allBenessere.emit(Result.Success(allServices))
            } catch (e: Exception) {
                Log.e(TAG,"Unexpected error during loading Benessere list", e)
                _allBenessere.emit(Result.Error(DataError.Network.UNKNOWN))
            }
        }
    }

    data class BenessereServices(
        val servicesBenessere: List<Service> = emptyList()
    )

    companion object {
        private const val TAG = "BenessereViewModel"
    }

     */
}