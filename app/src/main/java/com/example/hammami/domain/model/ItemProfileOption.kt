package com.example.hammami.domain.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import com.example.hammami.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemProfileOption(
    val title: String,
    @DrawableRes val leadingIconResId: Int,
    @DrawableRes val trailingIconResId: Int = R.drawable.ic_chevron_right,
    @IdRes val navigationDestination: Int
) : Parcelable