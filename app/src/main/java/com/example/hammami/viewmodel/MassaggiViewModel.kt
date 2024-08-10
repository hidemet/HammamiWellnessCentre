package com.example.hammami.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.data.Service
import com.example.hammami.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "MassaggiViewModel"

@HiltViewModel
class MassaggiViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
): ViewModel() {

    private val _allMassaggi = MutableStateFlow<Resource<List<Service>>>(Resource.Loading())
    val allMassaggi: StateFlow<Resource<List<Service>>> = _allMassaggi

    init {
        fetchAllMassaggi()
    }

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
}