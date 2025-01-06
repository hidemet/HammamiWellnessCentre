package com.example.hammami.data.datasource.payment

import com.example.hammami.domain.model.payment.CreditCardPayment
import kotlinx.coroutines.delay
import javax.inject.Inject


class CreditCardDataSource @Inject constructor() : PaymentDataSource<CreditCardPayment> {
    override suspend fun processPayment(
        paymentSystem: CreditCardPayment,
        amount: Double
    ): String {
        delay(500)
        return "CC_${paymentSystem.creditCard.number.takeLast(4)}_${System.currentTimeMillis()}"
    }

}