package com.example.hammami.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.data.Service
import com.example.hammami.util.Resource
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
): ViewModel() {

    private val _newServices = MutableStateFlow<Resource<List<Service>>>(Resource.Loading())
    val newServices: StateFlow<Resource<List<Service>>> = _newServices

    private val _bestDeals = MutableStateFlow<Resource<List<Service>>>(Resource.Loading())
    val bestDeals: StateFlow<Resource<List<Service>>> = _bestDeals

    private val _recommended = MutableStateFlow<Resource<List<Service>>>(Resource.Loading())
    val recommended: StateFlow<Resource<List<Service>>> = _recommended

    init {
        fetchNewServices()
     // fetchBestDeals()
       // fetchRecommended()
    }

    fun fetchNewServices() {
        viewModelScope.launch {
            _newServices.emit(Resource.Loading())
            val allServices = mutableListOf<Service>()

            try {
                val esteticaSnapshot = firestore.collection("/Servizi/Estetica/Trattamento corpo")
                    .whereEqualTo("Sezione homepage", "Novità").get().await()
                allServices.addAll(esteticaSnapshot.toObjects(Service::class.java))

                val benessereSnapshot = firestore.collection("/Servizi/Benessere/trattamenti")
                    .whereEqualTo("Sezione homepage", "Novità").get().await()
                allServices.addAll(benessereSnapshot.toObjects(Service::class.java))

                _newServices.emit(Resource.Success(allServices))
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel recupero dei nuovi servizi: ${e.message}", e)
                _newServices.emit(Resource.Error(e.message ?: "Si è verificato un errore sconosciuto"))
            }
        }
    }

    fun fetchBestDeals() {
        viewModelScope.launch {
            _bestDeals.emit(Resource.Loading())
        }

        firestore.collection("/Servizi/Benessere/trattamenti")
            .whereEqualTo("Sezione homepage", "Offerte").get().addOnSuccessListener { result ->
                val bestDealsList = result.toObjects(Service::class.java)
                viewModelScope.launch {
                    _bestDeals.emit(Resource.Success(bestDealsList))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _bestDeals.emit(Resource.Error(it.message ?: "Errore sconosciuto"))
                }
            }
    }

    fun fetchRecommended() {
        viewModelScope.launch {
            _recommended.emit(Resource.Loading())
        }

        firestore.collection("/Servizi/Benessere/trattamenti")
            .whereEqualTo("Sezione homepage", "Consigliati").get().addOnSuccessListener { result ->
                val recommendedList = result.toObjects(Service::class.java)
                viewModelScope.launch {
                    _recommended.emit(Resource.Success(recommendedList))
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _recommended.emit(Resource.Error(it.message ?: "Errore sconosciuto"))
                }
            }
    }
}