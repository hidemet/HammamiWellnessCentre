package com.example.hammami.viewmodel
/*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.data.Service
import com.example.hammami.database.FirebaseDb
import com.example.hammami.util.Resource
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class MainCategoryViewModel(
    private val firestore: FirebaseDb
): ViewModel() {

    private val _newServices = MutableStateFlow<Resource<List<Service>>>(Resource.Unspecified())
    val newServices: StateFlow<Resource<List<Service>>> = _newServices
*/
/*
    init {
        fetchNewServices()
    }

    fun fetchNewServices(){

        viewModelScope.launch {
            _newServices.emit(Resource.Loading())
        }

        firestore.collection("/Servizi/Benessere/trattamenti")
            .whereEqualTo("Sezione homepage", "Novità").get().addOnSuccessListener { result ->
                val newServicesList = result.toObjects(Service::class.java)
                viewModelScope.launch {
                    _newServices.emit(Resource.Success(newServicesList))
                }
            }.addOnFailureListener{
                viewModelScope.launch {
                    _newServices.emit(Resource.Error(it.message.toString()))
                }
            }
    }*//*

}*/
