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

private const val TAG = "ServizioViewModel"

@HiltViewModel
class ServizioViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
): ViewModel() {

    private val _allBenessere = MutableStateFlow<Resource<List<Service>>>(Resource.Loading())
    val allBenessere: StateFlow<Resource<List<Service>>> = _allBenessere

    init {
        getServizioById()
    }

    fun getServizioById() {
        viewModelScope.launch {
            val allServices = mutableListOf<Service>()

            try {
                val benessereSnapshot = firestore.collection("/Servizi/Benessere/trattamenti").get().await()
                allServices.addAll(benessereSnapshot.toObjects(Service::class.java))

                _allBenessere.emit(Resource.Success(allServices))
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel recupero dei nuovi servizi: ${e.message}", e)
                _allBenessere.emit(
                    Resource.Error(
                        e.message ?: "Si è verificato un errore sconosciuto"
                    )
                )
            }
        }
    }
}