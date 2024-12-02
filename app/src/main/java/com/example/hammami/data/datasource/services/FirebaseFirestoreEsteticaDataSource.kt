package com.example.hammami.data.datasource.services

import android.util.Log
import com.example.hammami.domain.model.Service
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreEsteticaDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val epilazioneCollection = firestore.collection("/Servizi/Estetica/Epilazione corpo con cera")
    private val trattamentoVisoCollection = firestore.collection("/Servizi/Estetica/Trattamento viso")
    private val trattamentoCorpoCollection = firestore.collection("/Servizi/Estetica/Trattamento corpo")


    suspend fun fetchEpilazioneData() : List<Service> {

        val allEpilazione = mutableListOf<Service>()

        try {
            allEpilazione.addAll(epilazioneCollection.get().await().toObjects(Service::class.java))
            Log.e("FirebaseFiresoreEsteticaDataSource", "Aggiunti i servizi di epilazione")
            return allEpilazione
            //_allEpilazione.emit(Resource.Success(allServices))
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun fetchTrattCorpoData() : List<Service> {

        val allTrattCorpo = mutableListOf<Service>()

        try {
            allTrattCorpo.addAll(trattamentoCorpoCollection.get().await().toObjects(Service::class.java))
            //_allTrattCorpo.emit(Resource.Success(allServices))
            Log.e("FirebaseFiresoreEsteticaDataSource", "Aggiunti i servizi di trattamento corpo")
            return allTrattCorpo
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun fetchTrattVisoData() : List<Service> {

        val allTrattViso = mutableListOf<Service>()

        try {
            allTrattViso.addAll(trattamentoVisoCollection.get().await().toObjects(Service::class.java))
            Log.e("FirebaseFiresoreEsteticaDataSource", "Aggiunti i servizi di trattamento viso")
            //_allTrattViso.emit(Resource.Success(allServices))
            return allTrattViso
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

}