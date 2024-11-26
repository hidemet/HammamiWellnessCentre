package com.example.hammami.domain.usecase

import com.example.hammami.domain.AvailableVoucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.domain.usecase.user.GetUserPointsUseCase
import com.example.hammami.core.result.Result
import com.example.hammami.domain.error.DataError
import javax.inject.Inject

class GetAvailableVouchersUseCase @Inject constructor(
    private val getUserPointsUseCase: GetUserPointsUseCase
) {
    suspend operator fun invoke(type: VoucherType): Result<List<AvailableVoucher>, DataError> {
        return when (type) {
            VoucherType.GIFT_CARD ->
                Result.Success(AvailableVoucher.createGiftCardOptions())

            VoucherType.COUPON -> {
                when (val pointsResult = getUserPointsUseCase()) {
                    is Result.Success ->
                        Result.Success(
                            AvailableVoucher.createPointRewardOptions(pointsResult.data)
                        )
                    is Result.Error -> Result.Error(pointsResult.error)
                }
            }
        }
    }
}