package com.example.hammami.domain.usecase.payment

import com.example.hammami.data.repositories.PaymentRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.payment.Discount
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.model.payment.PaymentSystem
import com.example.hammami.domain.usecase.giftcard.CreateGiftCardUseCase
import com.example.hammami.domain.usecase.validation.payment.ValidatePaymentSystemUseCase
import javax.inject.Inject

class ProcessPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val createGiftCardUseCase: CreateGiftCardUseCase
) {
    suspend operator fun invoke(
        amount: Double,
        paymentSystem: PaymentSystem,
    ): Result<String, DataError> {
        // Calcolo dell'importo finale
        return paymentRepository.processPayment(paymentSystem, amount)
    }
}