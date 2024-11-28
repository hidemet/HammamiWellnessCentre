package com.example.hammami.data.repositories

import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.BookingStatus
import com.example.hammami.domain.model.payment.PaymentItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.sql.Date
import java.sql.Timestamp
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    private val bookingsCollection = firestore.collection("bookings")

}