package com.example.hammami.core.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.hammami.R
import com.example.hammami.domain.model.BookingStatus
import com.google.android.material.chip.Chip

object BookingUiHelper {
    fun setupBookingStatusChip(chip: Chip, status: BookingStatus, context: Context) {
        chip.text = when (status) {
            BookingStatus.RESERVED -> context.getString(R.string.booking_status_reserved)
            BookingStatus.CONFIRMED -> context.getString(R.string.booking_status_confirmed)
            BookingStatus.COMPLETED -> context.getString(R.string.booking_status_completed)
            BookingStatus.CANCELED -> TODO()
        }
        chip.chipIcon = ContextCompat.getDrawable(context, when (status) {
            BookingStatus.RESERVED -> R.drawable.ic_check
            BookingStatus.CONFIRMED -> R.drawable.ic_check
            BookingStatus.COMPLETED -> R.drawable.ic_check
            BookingStatus.CANCELED -> TODO()
        })
    }
}