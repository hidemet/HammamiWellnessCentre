package com.example.hammami.domain.usecase


import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.model.VoucherType
import javax.inject.Inject

class GetUserVouchersUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(type : VoucherType): Result<List<Voucher>, DataError> =
        voucherRepository.getUserVouchersByType(type)
}
