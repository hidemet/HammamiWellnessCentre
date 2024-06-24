package com.example.hammami.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hammami.data.Service
import com.example.hammami.util.Resource
import com.google.android.play.integrity.internal.c
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainCategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
): ViewModel() {

    private val _newServices = MutableStateFlow<Resource<List<Service>>>(Resource.Unspecified())
    val newServices: StateFlow<Resource<List<Service>>> = _newServices

    fun fetchNewServices(){
        firestore.collection("/Servizi/Benessere/trattamenti")
    }
}