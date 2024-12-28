package com.example.hammami.data.repositories

import com.example.hammami.data.datasource.payment.CreditCardDataSource
import com.example.hammami.data.datasource.payment.GooglePayDataSource
import com.example.hammami.data.datasource.payment.PayPalDataSource
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.core.utils.KarmaPointsCalculator
import com.example.hammami.domain.factory.VoucherFactory
import com.example.hammami.domain.model.BookingStatus
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.domain.model.payment.CreditCardPayment
import com.example.hammami.domain.model.payment.GooglePayPayment
import com.example.hammami.domain.model.payment.PayPalPayment
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.model.payment.PaymentSystem
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val creditCardDataSource: CreditCardDataSource,
    private val payPalDataSource: PayPalDataSource,
    private val googlePayDataSource: GooglePayDataSource,
    private val voucherRepository: VoucherRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore,
    private val voucherFactory: VoucherFactory,
    private val karmaPointsCalculator: KarmaPointsCalculator,
) {
    suspend fun processPaymentTransaction(
        paymentSystem: PaymentSystem,
        paymentItem: PaymentItem,
        appliedVoucher: Voucher?,
        amount: Double,
    ): Result<String, DataError> {

        val userId = when (val result = authRepository.getCurrentUserId()) {
            is Result.Success -> result.data
            is Result.Error -> return Result.Error(result.error)
        }

        val user = when (val result = userRepository.getUserData(userId)) {
            is Result.Success -> result.data
            is Result.Error -> return Result.Error(result.error)
        }


        return try {
            val transactionId = when (paymentSystem) {
                is CreditCardPayment -> creditCardDataSource.processPayment(paymentSystem, amount)
                is GooglePayPayment -> googlePayDataSource.processPayment(paymentSystem, amount)
                is PayPalPayment -> payPalDataSource.processPayment(paymentSystem, amount)
                else -> throw IllegalArgumentException("Sistema di pagamento non supportato")
            }

            firestore.runTransaction { transaction ->
                // 1. Gestisco il voucher se presente
                appliedVoucher?.let { voucher ->
                    when (val result = voucherRepository.deleteVoucher(transaction, voucher.code)) {
                        is Result.Error -> return@runTransaction result
                        is Result.Success -> Unit
                    }
                }

                // 2. Calcolo e aggiungo i punti karma
                val earnedPoints = karmaPointsCalculator.calculatePoints(amount, paymentItem)
                val userDocRef = firestore.collection("users").document(userId)
                transaction.update(userDocRef, "points", user.points + earnedPoints)


                // 3. Creo il documento appropriato in base al tipo di acquisto
                when (paymentItem) {
                    is PaymentItem.GiftCardPayment -> {
                        val newVoucher = voucherFactory.createVoucher(
                            userId = userId,
                            value = amount,
                            type = VoucherType.GIFT_CARD,
                            transactionId = transactionId
                        )
                        when (val result =
                            voucherRepository.createVoucherDocument(transaction, newVoucher)) {
                            is Result.Error -> return@runTransaction result
                            is Result.Success -> Unit
                        }
                    }

                    is PaymentItem.ServiceBookingPayment -> {
                        val bookingId = paymentItem.bookingId
                        val bookingRef = firestore.collection("bookings").document(bookingId)
                        transaction.update(bookingRef, "status", BookingStatus.CONFIRMED)
                    }
                }
                // 3. Restituisco l'ID transazione
                transactionId
            }.await()

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
