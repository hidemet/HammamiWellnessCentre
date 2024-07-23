package com.example.hammami.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.data.Service
import com.example.hammami.util.Resource
import com.google.firebase.firestore.DocumentReference
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private val TAG = "MainCategoryViewModel"

@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _newServices = MutableStateFlow<Resource<List<Service>>>(Resource.Unspecified())
    val newServices: StateFlow<Resource<List<Service>>> = _newServices

    private val _bestDeals = MutableStateFlow<Resource<List<Service>>>(Resource.Unspecified())
    val bestDeals: StateFlow<Resource<List<Service>>> = _bestDeals

    private val _recommended = MutableStateFlow<Resource<List<Service>>>(Resource.Unspecified())
    val recommended: StateFlow<Resource<List<Service>>> = _recommended

    init {
        fetchNewServices()
        // fetchBestDeals()
        //fetchRecommended()
    }

    fun fetchNewServices() {
        viewModelScope.launch {
            _newServices.emit(Resource.Loading())
        }

        firestore.collection("Servizi").document("Benessere").collection("trattamenti").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "Raw data: ${document.data}")
                }
                // val newServicesList = result.toObjects(Service::class.java)
                val newServicesList = result.documents.mapNotNull { documentSnapshot ->
                    try {
                        val category = documentSnapshot.get("Categoria")
                        val categoryList = when (category) {
                            is String -> listOf(category)
                            is List<*> -> category.filterIsInstance<String>()
                            else -> null
                        }
                        Service(
                            id = documentSnapshot.getString("id") ?: "",
                            name = documentSnapshot.getString("Nome") ?: "",
                            price = documentSnapshot.getDouble("Prezzo")?.toFloat(),
                            discountPrice = documentSnapshot.getDouble("PrezzoScontato")?.toFloat(),
                            description = documentSnapshot.getString("Descrizione") ?: "",
                            image = documentSnapshot.get("Immagine") as? DocumentReference,
                            length = documentSnapshot.getLong("Durata"),
                            category = categoryList,
                            reviews = documentSnapshot.get("Recensioni") as? List<DocumentReference>,
                            homepageSection = documentSnapshot.getString("Sezione homepage"),
                            benefits = documentSnapshot.getString("Benefici")
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializing document: ${documentSnapshot.id}", e)
                        null
                    }
                }
                viewModelScope.launch {
                    _newServices.emit(Resource.Success(newServicesList))
                }
            }.addOnFailureListener {
            viewModelScope.launch {
                _newServices.emit(Resource.Error(it.message ?: "Errore sconosciuto"))
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