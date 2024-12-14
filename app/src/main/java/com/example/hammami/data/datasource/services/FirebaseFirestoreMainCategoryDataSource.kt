package com.example.hammami.data.datasource.services

import android.util.Log
import com.example.hammami.domain.model.Review
import com.example.hammami.domain.model.Service
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.Typography.section

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

    suspend fun getIdServiceFromName(name: String): String? {

        val collections = listOf(
            "/Servizi/Estetica/Trattamento corpo",
            "/Servizi/Estetica/Epilazione corpo con cera",
            "/Servizi/Estetica/Trattamento viso",
            "/Servizi/Benessere/trattamenti",
            "/Servizi/Massaggi/trattamenti"
        )

        for (collection in collections) {
            val snapshot = firestore.collection(collection)
                .whereEqualTo("Nome", name)
                .get()
                .await()
            if (snapshot.documents.isNotEmpty()) {
                //return snapshot.documents.first().id
                return snapshot.documents.first().reference.path
            }

        }

        Log.e("FirebaseFirestoreMainCategoryDataSource", "NON HO TROVATO NULLA")
        return null

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

    suspend fun addReviewToAService(servicePath: String, reviewId: String){

        /*
        val collections = listOf(
            "/Servizi/Estetica/Trattamento corpo",
            "/Servizi/Estetica/Epilazione corpo con cera",
            "/Servizi/Estetica/Trattamento viso",
            "/Servizi/Benessere/trattamenti",
            "/Servizi/Massaggi/trattamenti"
        )



        for (collection in collections) {
            val documentSnapshot = firestore.collection(collection)
                .document(serviceId)
                .get()
                .await()

            if (documentSnapshot.exists()) {
                firestore.collection(collection)
                    .document(serviceId)
                    .update("Recensioni", FieldValue.arrayUnion(reviewId))
                    .await()
            }
        }

         */

        Log.e("FirebaseFirestoreMainCategoryDataSource", "servicePath: $servicePath")

        val reviewToAdd = firestore.document(reviewId)

        firestore.document(servicePath)
            .update("Recensioni", FieldValue.arrayUnion(reviewToAdd))
            .await()

    }

    suspend fun getCollectionPathFromServiceId(serviceId: String): String {
        val collections = listOf(
            "/Servizi/Estetica/Trattamento corpo",
            "/Servizi/Estetica/Epilazione corpo con cera",
            "/Servizi/Estetica/Trattamento viso",
            "/Servizi/Benessere/trattamenti",
            "/Servizi/Massaggi/trattamenti"
        )

        for (collection in collections) {
            val documentSnapshot = firestore.collection(collection)
                .document(serviceId)
                .get()
                .await()

            if (documentSnapshot.exists()) {
                return collection
            }
        }

        return ""
    }

    suspend fun getPathFromServiceId(serviceId: String): String {
        val collections = listOf(
            "/Servizi/Estetica/Trattamento corpo",
            "/Servizi/Estetica/Epilazione corpo con cera",
            "/Servizi/Estetica/Trattamento viso",
            "/Servizi/Benessere/trattamenti",
            "/Servizi/Massaggi/trattamenti"
        )

        for (collectionPath in collections) {
            val docRef = firestore.document("$collectionPath/$serviceId")
            docRef.get().await()
            return docRef.path
        }

        return ""
    }
}