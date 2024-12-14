package com.example.hammami.data.datasource.payment

import com.example.hammami.domain.model.payment.PaymentSystem

interface PaymentDataSource<in T : PaymentSystem> {
    suspend fun processPayment(paymentSystem: T, amount: Double): String
}
