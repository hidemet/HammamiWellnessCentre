package com.example.hammami.presentation.model

import android.icu.text.SimpleDateFormat
import android.os.Parcelable
import com.example.hammami.domain.model.giftCard.GiftCard
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.Locale

@Parcelize
data class GiftCardUi(
    val code: String,
    val value: Double,
    val formattedExpiryDate: String,
    val isValid: Boolean
) : Parcelable

// Extension function per il mapping
fun GiftCard.toUiModel() = GiftCardUi(
    code = code,
    value = value,
    formattedExpiryDate = expirationDate.toDate().format(),
    isValid = isValid()
)

private fun Date.format(): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this)