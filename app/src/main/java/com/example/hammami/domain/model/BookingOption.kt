package com.example.hammami.domain.model

import androidx.annotation.DrawableRes
import com.example.hammami.presentation.ui.features.shared.booking.BookingDetailViewModel.*

data class BookingOption(
    val title: String,
    @DrawableRes val iconResId: Int,
    val type: OptionType
)