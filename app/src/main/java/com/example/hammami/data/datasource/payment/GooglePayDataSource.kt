package com.example.hammami.data.datasource.payment

import com.example.hammami.domain.model.payment.GooglePayPayment
import kotlinx.coroutines.delay
import javax.inject.Inject

class GooglePayDataSource @Inject constructor() : PaymentDataSource<GooglePayPayment> {
    override suspend fun processPayment(
        paymentSystem: GooglePayPayment,
        amount: Double
    ): String {
        delay(1000)
        return "GP_${System.currentTimeMillis()}"
    }
}