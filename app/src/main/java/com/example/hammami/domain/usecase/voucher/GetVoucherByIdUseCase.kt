package com.example.hammami.domain.usecase.voucher

import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Voucher
import javax.inject.Inject

class GetVoucherByIdUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(id: String): Result<Voucher, DataError> {
        return voucherRepository.getVoucherById(id)
    }
}