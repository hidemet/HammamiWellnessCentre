package com.example.hammami.data.datasource.services

import android.util.Log
import com.example.hammami.domain.model.Service
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreMainCategoryDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    suspend fun fetchNewServices() : List<Service> {
        try {
            Log.e("FirebaseFirestoreMainCategoryDataSource", "fetchNewServices")
            return fetchServicesForSection("Novità")
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
        }

    suspend fun fetchBestDeals() : List<Service> {
        try {
            Log.e("FirebaseFirestoreMainCategoryDataSource", "fetchBestDeals")
            return fetchServicesForSection("Offerte")
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun fetchRecommended() : List<Service> {
        try {
            Log.e("FirebaseFirestoreMainCategoryDataSource", "fetchRecommended")
            return fetchServicesForSection("Consigliati")
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    private suspend fun fetchServicesForSection(section: String): List<Service>{
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

    /*
    suspend fun fetchBenessereData(serviceId: String) : Service {

        try {
            return benessereCollection.document(serviceId).get().await().toObjects(Service::class.java)
            //_allBenessere.emit(Result.Success(allServices))
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }
     */

}