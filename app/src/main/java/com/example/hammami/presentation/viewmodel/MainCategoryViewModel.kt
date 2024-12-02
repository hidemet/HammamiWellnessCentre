package com.example.hammami.presentation.viewmodel


import androidx.lifecycle.ViewModel

import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.domain.model.Service
import com.example.hammami.core.result.Result
import com.example.hammami.domain.usecase.GetBestDealsUseCase
import com.example.hammami.domain.usecase.GetNewServicesUseCase
import com.example.hammami.domain.usecase.GetRecommendedUseCase
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "MainCategoryViewModel"

@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val getBestDealsUseCase: GetBestDealsUseCase,
    private val getNewServicesUseCase: GetNewServicesUseCase,
    private val getRecommendedUseCase: GetRecommendedUseCase
) : ViewModel() {

    private val supervisorJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(supervisorJob + Dispatchers.Main)

    private val _allBestDeals = MutableStateFlow(MainCategoryServices())
    val allBestDeals = _allBestDeals.asStateFlow()

    private val _allRecommended = MutableStateFlow(MainCategoryServices())
    val allRecommended = _allRecommended.asStateFlow()

    private val _allNewServices = MutableStateFlow(MainCategoryServices())
    val allNewServices = _allNewServices.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        //fetchAllBenessere()
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                //Log.e(TAG, "INIZIO A CARICARE LE NOVITÀ: ")
                val newServicesDeferred = async { loadNewServicesData() }
                //Log.e(TAG, "INIZIO A CARICARE I RACCOMANDATI: ")
                val recommendedDeferred = async { loadRecommendedData() }
                //Log.e(TAG, "INIZIO A CARICARE LE OFFERTE: ")
                val bestDealsDeferred = async { loadBestDealsData() }

                newServicesDeferred.await()
                recommendedDeferred.await()
                bestDealsDeferred.await()
            } catch (e: Exception) {
                //Log.e(TAG, "Errore durante il caricamento dei dati: ${e.message}", e)
                emitUiEvent(UiEvent.ShowError(UiText.StringResource(R.string.benvenuto_)))
            }
        }
    }

    private suspend fun loadBestDealsData(){
        when (val resultBestDeals = getBestDealsUseCase()) {
            is Result.Success -> {
                //Log.e(TAG, "Sono nel loadBestDealsData, SUCCESSO")
                _allBestDeals.update { it.copy(servicesBestDeals = resultBestDeals.data) }
            }
            is Result.Error -> {
                //Log.e(TAG, "Sono nel loadBestDealsData, FALLITO")
                emitUiEvent(UiEvent.ShowError(resultBestDeals.error.asUiText()))
            }

        }
    }

    private suspend fun loadNewServicesData(){
        when (val resultNewServices = getNewServicesUseCase()) {
            is Result.Success -> {
                //Log.e(TAG, "Sono nel loadNewServicesData, SUCCESSO")
                _allNewServices.update { it.copy(servicesNewServices = resultNewServices.data) }
            }
            is Result.Error -> {
                //Log.e(TAG, "Sono nel loadNewServicesData, FALLITO")
                emitUiEvent(UiEvent.ShowError(resultNewServices.error.asUiText()))
            }

        }
    }

    private suspend fun loadRecommendedData(){
        when (val resultRecommended = getRecommendedUseCase()) {
            is Result.Success -> {
                //Log.e(TAG, "Sono nel loadRecommendedData, SUCCESSO")
                _allRecommended.update { it.copy(servicesRecommended = resultRecommended.data) }
            }
            is Result.Error -> {
                //Log.e(TAG, "Sono nel loadRecommendedData, FALLITO")
                emitUiEvent(UiEvent.ShowError(resultRecommended.error.asUiText()))
            }

        }
    }

    override fun onCleared() {
        super.onCleared()
        supervisorJob.cancel()
    }

    data class MainCategoryServices(
        val servicesRecommended: List<Service> = emptyList(),
        val servicesBestDeals: List<Service> = emptyList(),
        val servicesNewServices: List<Service> = emptyList()
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

    private val _newServices = MutableStateFlow<Result<List<Service>, DataError>>(Result.Success(emptyList()))
    val newServices: StateFlow<Result<List<Service>, DataError>> = _newServices

    private val _bestDeals = MutableStateFlow<Result<List<Service>, DataError>>(Result.Success(emptyList()))
    val bestDeals: StateFlow<Result<List<Service>, DataError>> = _bestDeals

    private val _recommended = MutableStateFlow<Result<List<Service>, DataError>>(Result.Success(emptyList()))
    val recommended: StateFlow<Result<List<Service>, DataError>> = _recommended

    init {
        fetchNewServices()
        fetchBestDeals()
        fetchRecommended()
    }

    fun fetchNewServices() {
        viewModelScope.launch {
            try {
                val allServices = fetchServicesForSection("Novità")
                _newServices.emit(Result.Success(allServices))
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel recupero dei nuovi servizi: ${e.message}", e)
                _newServices.emit(Result.Error(DataError.Network.UNKNOWN))
            }
        }
    }

    fun fetchBestDeals() {
        viewModelScope.launch {
            try {
                val allServices = fetchServicesForSection("Offerte")
                _bestDeals.emit(Result.Success(allServices))
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel recupero delle migliori offerte: ${e.message}", e)
                _bestDeals.emit(Result.Error(DataError.Network.UNKNOWN))
            }
        }
    }

    fun fetchRecommended() {
        viewModelScope.launch {
            try {
                val allServices = fetchServicesForSection("Consigliati")
                _recommended.emit(Result.Success(allServices))
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel recupero dei servizi consigliati: ${e.message}", e)
                _recommended.emit(Result.Error(DataError.Network.UNKNOWN))
            }
        }
    }

    private suspend fun fetchServicesForSection(section: String): List<Service> {
        val allServices = mutableListOf<Service>()
        val collections = listOf(
            "/Servizi/Estetica/Trattamento corpo",
            "/Servizi/Estetica/Epilazione corpo con cera",
            "/Servizi/Estetica/Trattamento viso",
            "/Servizi/Benessere/trattamenti",
            "/Servizi/Massaggi/trattamenti"
        )

        for (collection in collections) {
            val snapshot = firestore.collection(collection)
                .whereEqualTo("Sezione homepage", section)
                .get()
                .await()
            allServices.addAll(snapshot.toObjects(Service::class.java))
        }

        return allServices
    }

     */
}