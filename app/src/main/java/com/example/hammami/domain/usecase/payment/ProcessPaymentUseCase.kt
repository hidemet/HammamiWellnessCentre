package com.example.hammami.domain.usecase.payment

import com.example.hammami.data.repositories.PaymentRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.model.payment.PaymentSystem
import javax.inject.Inject

class ProcessPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(
        paymentSystem: PaymentSystem,
        amount: Double,
        appliedVoucher: Voucher? = null
    ): Result<String, DataError> {
        val finalAmount = appliedVoucher?.let { amount - it.value } ?: amount

        return when (val paymentResult = paymentRepository.processPayment(paymentSystem, finalAmount)) {
            is Result.Success -> {
                // Elimina il voucher solo se il pagamento ha successo
                if (appliedVoucher != null) {
                    when (val voucherResult = voucherRepository.deleteVoucher(appliedVoucher.code)) {
                        is Result.Success -> Result.Success(paymentResult.data)
                        is Result.Error -> Result.Error(voucherResult.error)
                    }
                } else {
                    Result.Success(paymentResult.data)
                }
            }
            is Result.Error -> Result.Error(paymentResult.error)
        }
    }
}