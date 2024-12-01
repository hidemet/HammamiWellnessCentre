package com.example.hammami.domain.usecase

import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.error.DataError
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.hammami.core.result.Result
import com.example.hammami.domain.factory.VoucherFactory


import javax.inject.Inject

class CreateVoucherUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val voucherFactory: VoucherFactory
) {
    suspend operator fun invoke(
        value: Double,
        type: VoucherType,
        transactionId: String? = null
    ): Result<Voucher, DataError> {
        return when (val userResult = getCurrentUserIdUseCase()) {
            is Result.Success -> {
                voucherRepository.saveVoucher(
                    voucherFactory.createVoucher(
                        value,
                        type,
                        userResult.data,
                        transactionId
                    )
                )
            }
            is Result.Error -> Result.Error(userResult.error)
        }
    }
}