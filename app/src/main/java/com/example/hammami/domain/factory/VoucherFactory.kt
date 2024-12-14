package com.example.hammami.domain.factory

import android.util.Log
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.domain.model.Voucher
import com.google.firebase.Timestamp
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VoucherFactory @Inject constructor() {
    fun createVoucher(
        userId: String,
        value: Double,
        type: VoucherType,
        transactionId: String? = null,
    ): Voucher {
        return Voucher(
            userId = userId,
            code = generateVoucherCode(value, type),
            value = value,
            type = type,
            expirationDate = getExpirationDate(),
            creationTransactionId = transactionId,
        )
    }

    private fun generateVoucherCode(value: Double, type: VoucherType): String {
        val prefix = when(type) {
            VoucherType.GIFT_CARD -> "GC"
            VoucherType.COUPON -> "CO"
        }
        val uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12).uppercase()
        Log.d("DiscountVoucher", "generateCode: $prefix$uuid${value.toInt()}")
        return "$prefix$uuid${value.toInt()}"
    }

    private fun getExpirationDate(): Timestamp =
        Timestamp(Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365)))
}