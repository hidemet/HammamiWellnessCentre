package com.example.hammami.domain.usecase.voucher

import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Voucher
import com.example.hammami.core.result.Result

import javax.inject.Inject

class ValidateGlobalVoucherUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(code: String, amount: Double): Result<Voucher, DataError> {
        return when (val voucherResult = voucherRepository.getVoucherByCode(code)) {
            is Result.Success -> {
                val voucher = voucherResult.data ?: return Result.Error(DataError.Voucher.NOT_FOUND)

                when {
                    voucher.isExpired -> Result.Error(DataError.Voucher.EXPIRED)
                    voucher.value > amount -> Result.Error(DataError.Voucher.VALUE_EXCEEDS_AMOUNT)
                    else -> Result.Success(voucher)
                }
            }
            is Result.Error -> voucherResult
        }
    }
}