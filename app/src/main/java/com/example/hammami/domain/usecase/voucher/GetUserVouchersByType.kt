package com.example.hammami.domain.usecase.voucher


import com.example.hammami.domain.error.DataError
import com.example.hammami.core.result.Result
import com.example.hammami.data.repositories.VoucherRepository
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.domain.usecase.user.GetCurrentUserIdUseCase
import javax.inject.Inject

class GetUserVouchersByType @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend operator fun invoke(type: VoucherType): Result<List<Voucher>, DataError> {
        return when (val userResult = getCurrentUserIdUseCase()) {
            is Result.Success -> voucherRepository.getUserVouchersByType(userResult.data, type)
            is Result.Error -> Result.Error(userResult.error)
        }
    }
}