package com.example.hammami.data.datasource.payment

import com.example.hammami.domain.model.payment.PayPalPayment
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject


class PayPalDataSource @Inject constructor() : PaymentDataSource<PayPalPayment> {
    override suspend fun processPayment(
        paymentSystem: PayPalPayment,
        amount: Double
    ): String {
        delay(1500)

        return "PP_${UUID.randomUUID()}"

    }
}