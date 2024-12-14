package com.example.hammami.data.repositories

import android.net.http.NetworkException
import com.example.hammami.data.datasource.payment.CreditCardDataSource
import com.example.hammami.data.datasource.payment.GooglePayDataSource
import com.example.hammami.data.datasource.payment.PayPalDataSource
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.payment.CreditCardPayment
import com.example.hammami.domain.model.payment.GooglePayPayment
import com.example.hammami.domain.model.payment.PayPalPayment
import com.example.hammami.domain.model.payment.PaymentSystem
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val creditCardDataSource: CreditCardDataSource,
    private val payPalDataSource: PayPalDataSource,
    private val googlePayDataSource: GooglePayDataSource
) {
    suspend fun processPayment(
        paymentSystem: PaymentSystem,
        amount: Double
    ): Result<String, DataError> {
        return try {
            val transactionId = when (paymentSystem) {
                is CreditCardPayment -> creditCardDataSource.processPayment(paymentSystem, amount)
                is GooglePayPayment -> googlePayDataSource.processPayment(paymentSystem, amount)
                is PayPalPayment -> payPalDataSource.processPayment(paymentSystem, amount)
                else -> {
                    throw IllegalArgumentException("Unsupported payment system")
                }
            }
            Result.Success(transactionId)
        } catch (e: Exception) {
            Result.Error(mapToPaymentError(e))
        }
    }

    private fun mapToPaymentError(e: Exception): DataError = when (e) {
        is FirebaseFirestoreException -> DataError.Network.SERVER_ERROR
        is FirebaseNetworkException -> DataError.Network.NO_INTERNET
//    is PaymentProviderException -> when (e) {
//        is PaymentProviderException.CardDeclined -> DataError.Payment.PAYMENT_DECLINED
//        is PaymentProviderException.InvalidCard -> DataError.Payment.INVALID_CARD
//        is PaymentProviderException.ExpiredCard -> DataError.Payment.EXPIRED_CARD
//        is PaymentProviderException.InsufficientFunds -> DataError.Payment.INSUFFICIENT_FUNDS
//        else -> {}
//    }
        else -> DataError.Payment.UNKNOWN
    }
}
