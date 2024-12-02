package com.example.hammami.data.datasource.services

import com.example.hammami.domain.model.Service
import com.example.hammami.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreMassaggiDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val massaggiCollection = firestore.collection("/Servizi/Massaggi/trattamenti")

    suspend fun saveMassaggiInformation(userUid: String, user: User) {
        try {
            massaggiCollection.document(userUid).set(user).await()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun fetchMassaggiData() : List<Service> {
        val allMassaggi = mutableListOf<Service>()

        try {
            allMassaggi.addAll(massaggiCollection.get().await().toObjects(Service::class.java))
            //_allBenessere.emit(Result.Success(allServices))
            return allMassaggi
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

}