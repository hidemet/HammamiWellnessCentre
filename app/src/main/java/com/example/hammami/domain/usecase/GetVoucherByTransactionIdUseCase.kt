package com.example.hammami.domain.usecase

import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.model.Voucher
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class GetVoucherByTransactionIdUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(transactionId: String): Result<Voucher, DataError> {
        return voucherRepository.getVoucherByTransactionId(transactionId)
    }
}