package com.example.hammami.domain.usecase

import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.model.Voucher
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError

import javax.inject.Inject

class GetVoucherByCodeUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(code: String): Result<Voucher, DataError> =
        voucherRepository.getVoucherByCode(code)
}
