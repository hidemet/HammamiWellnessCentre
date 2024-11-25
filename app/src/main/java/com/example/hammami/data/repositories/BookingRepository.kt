package com.example.hammami.data.repositories

import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.Service
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result

class BookingRepository {

  fun  createBooking(serviceId: String, transactionId: String): Result<Booking, DataError> {
        // Creazione della prenotazione
      TODO()
    }
}