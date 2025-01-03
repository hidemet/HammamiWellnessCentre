package com.example.hammami.data.datasource.appointment

import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAppointmentDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val appointmentsCollection = firestore.collection("/Appointment")

    suspend fun saveReviewsInformation(userUid: String, user: User) {
        try {
            appointmentsCollection.document(userUid).set(user).await()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun fetchNewAppointmentData(clientEmail: String) : List<Booking> {
        val allNewAppointments = mutableListOf<Booking>()
        try {
                val querySnapshot = appointmentsCollection
                    .whereEqualTo("Cliente", clientEmail)
                    .whereEqualTo("isExpired", false)
                    .get()
                    .await()
                allNewAppointments.addAll(querySnapshot.toObjects(Booking::class.java))
            //allAppointments.addAll(appointmentsCollection.get().await().toObjects(ServiceAppointment::class.java))
            return allNewAppointments
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun fetchPastAppointmentData(clientEmail: String) : List<Booking> {
        val allPastAppointments = mutableListOf<Booking>()
        try {
            val querySnapshot = appointmentsCollection
                .whereEqualTo("Cliente", clientEmail)
                .whereEqualTo("isExpired", true)
                .get()
                .await()
            allPastAppointments.addAll(querySnapshot.toObjects(Booking::class.java))
            //allAppointments.addAll(appointmentsCollection.get().await().toObjects(ServiceAppointment::class.java))
            return allPastAppointments
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

}