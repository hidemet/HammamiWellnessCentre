package com.example.hammami.data.datasource.services

import com.example.hammami.domain.model.Service
import com.example.hammami.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreBenessereDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val benessereCollection = firestore.collection("/Servizi/Benessere/trattamenti")

    suspend fun saveBenessereInformation(userUid: String, user: User) {
        try {
            benessereCollection.document(userUid).set(user).await()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun fetchBenessereData() : List<Service> {
        val allServices = mutableListOf<Service>()
        try {
            allServices.addAll(benessereCollection.get().await().toObjects(Service::class.java))
            return allServices
            //_allBenessere.emit(Result.Success(allServices))
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun fetchBenessereData(serviceId: String) : Service {
        try {
            return benessereCollection.document(serviceId).get().await().toObject(Service::class.java)!!
            //_allBenessere.emit(Result.Success(allServices))
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

}