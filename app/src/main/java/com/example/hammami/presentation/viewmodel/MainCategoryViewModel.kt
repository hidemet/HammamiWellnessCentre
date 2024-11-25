package com.example.hammami.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.domain.model.Service
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "MainCategoryViewModel"

@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

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
}