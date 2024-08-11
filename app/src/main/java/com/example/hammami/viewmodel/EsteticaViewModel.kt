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

private const val TAG = "EsteticaViewModel"

@HiltViewModel
class EsteticaViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
): ViewModel() {

    private val _allEpilazione = MutableStateFlow<Resource<List<Service>>>(Resource.Loading())
    val allEpilazione: StateFlow<Resource<List<Service>>> = _allEpilazione

    private val _allTrattCorpo = MutableStateFlow<Resource<List<Service>>>(Resource.Loading())
    val allTrattCorpo: StateFlow<Resource<List<Service>>> = _allTrattCorpo

    private val _allTrattViso = MutableStateFlow<Resource<List<Service>>>(Resource.Loading())
    val allTrattViso: StateFlow<Resource<List<Service>>> = _allTrattViso

    init {
        fetchAllEpilazione()
        fetchAllTrattCorpo()
        fetchAllTrattViso()
    }

    fun fetchAllEpilazione() {
        viewModelScope.launch {
            val allServices = mutableListOf<Service>()

            try {
                val epilazioneSnapshot = firestore.collection("/Servizi/Estetica/Epilazione corpo con cera").get().await()
                allServices.addAll(epilazioneSnapshot.toObjects(Service::class.java))

                _allEpilazione.emit(Resource.Success(allServices))
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel recupero dei nuovi servizi: ${e.message}", e)
                _allEpilazione.emit(
                    Resource.Error(
                        e.message ?: "Si è verificato un errore sconosciuto"
                    )
                )
            }
        }
    }

    fun fetchAllTrattCorpo() {
        viewModelScope.launch {
            val allServices = mutableListOf<Service>()

            try {
                val trattCorpoSnapshot = firestore.collection("/Servizi/Estetica/Trattamento corpo").get().await()
                allServices.addAll(trattCorpoSnapshot.toObjects(Service::class.java))

                _allTrattCorpo.emit(Resource.Success(allServices))
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel recupero dei nuovi servizi: ${e.message}", e)
                _allTrattCorpo.emit(
                    Resource.Error(
                        e.message ?: "Si è verificato un errore sconosciuto"
                    )
                )
            }
        }
    }

    fun fetchAllTrattViso() {
        viewModelScope.launch {
            val allServices = mutableListOf<Service>()

            try {
                val trattVisoSnapshot = firestore.collection("/Servizi/Estetica/Trattamento viso").get().await()
                allServices.addAll(trattVisoSnapshot.toObjects(Service::class.java))

                _allTrattViso.emit(Resource.Success(allServices))
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel recupero dei nuovi servizi: ${e.message}", e)
                _allTrattViso.emit(
                    Resource.Error(
                        e.message ?: "Si è verificato un errore sconosciuto"
                    )
                )
            }
        }
    }
}