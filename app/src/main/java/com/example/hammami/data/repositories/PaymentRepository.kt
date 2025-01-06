package com.example.hammami.data.repositories

import android.content.Context
import android.util.Log
import com.example.hammami.data.datasource.payment.CreditCardDataSource
import com.example.hammami.data.datasource.payment.GooglePayDataSource
import com.example.hammami.data.datasource.payment.PayPalDataSource
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.core.utils.KarmaPointsCalculator
import com.example.hammami.core.utils.asUiText
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val creditCardDataSource: CreditCardDataSource,
    private val payPalDataSource: PayPalDataSource,
    private val googlePayDataSource: GooglePayDataSource,
    private val voucherRepository: VoucherRepository,
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore,
    private val voucherFactory: VoucherFactory,
    private val karmaPointsCalculator: KarmaPointsCalculator,
    @ApplicationContext private val context: Context
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

        return try {
            val transactionId = when (paymentSystem) {
                is CreditCardPayment -> creditCardDataSource.processPayment(paymentSystem, amount)
                is GooglePayPayment -> googlePayDataSource.processPayment(paymentSystem, amount)
                is PayPalPayment -> payPalDataSource.processPayment(paymentSystem, amount)
                else -> throw IllegalArgumentException("Sistema di pagamento non supportato")
            }

            val result: String = firestore.runTransaction { transaction ->
                // 1. Gestisco il voucher se presente
                appliedVoucher?.let { voucher ->
                    when (val deleteVoucherresult =
                        voucherRepository.deleteVoucher(transaction, voucher.code)) {
                        is Result.Error -> throw Exception(
                            deleteVoucherresult.error.asUiText().asString(context)
                        )

                        is Result.Success -> Unit
                    }
                }

                // 2. Calcolo e aggiungo i punti karma
                val earnedPoints = karmaPointsCalculator.calculatePoints(amount)
                when (val updatePointsResult =
                    userRepository.addUserPoints(
                        transaction,
                        userId,
                        earnedPoints
                    )) {
                    is Result.Error -> throw Exception(
                        updatePointsResult.error.asUiText().asString(context)
                    )

                    is Result.Success -> Unit
                }

                // 3. Creo il documento appropriato in base al tipo di acquisto
                when (paymentItem) {
                    is PaymentItem.GiftCardPayment -> {
                        val newVoucher = voucherFactory.createVoucher(
                            userId = userId,
                            value = amount,
                            type = VoucherType.GIFT_CARD,
                            transactionId = transactionId
                        )
                        when (val createVoucherResult = voucherRepository.createVoucherDocument(
                            transaction,
                            newVoucher
                        )) {
                            is Result.Error -> throw Exception(
                                createVoucherResult.error.asUiText().asString(context)
                            )

                            is Result.Success -> return@runTransaction createVoucherResult.data
                        }
                    }

                    is PaymentItem.ServiceBookingPayment -> {
                        val bookingId = paymentItem.bookingId
                        Log.d("PaymentRepository", "Updating booking with bookingId: $bookingId")
                        when (val resultUpdateBooking = bookingRepository.updateBooking(
                            transaction,
                            bookingId,
                            BookingStatus.CONFIRMED,
                            amount,
                            transactionId
                        )) {
                            is Result.Error -> throw Exception(
                                resultUpdateBooking.error.asUiText().asString(context)
                            )

                            is Result.Success -> return@runTransaction bookingId
                        }
                    }
                }
            }.await()
            Result.Success(result)

        } catch (e: Exception) {
            Result.Error(mapToPaymentError(e))
        }
    }

    private fun mapToPaymentError(e: Exception): DataError = when (e) {
        is FirebaseFirestoreException -> DataError.Network.SERVER_ERROR
        is FirebaseNetworkException -> DataError.Network.NO_INTERNET
        else -> DataError.Payment.UNKNOWN
    }
}
