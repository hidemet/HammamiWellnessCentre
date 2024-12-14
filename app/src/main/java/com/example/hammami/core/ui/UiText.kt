package com.example.hammami.core.ui

import android.content.Context
import androidx.annotation.StringRes

sealed class UiText {
    // Dynamic string provengono da serves, dalle Api
    data class DynamicString(val value: String) : UiText()

    // StringResource non passiamo la stringa ma l'id della stringa e possibili argomenti
    class StringResource(
        @StringRes val id: Int,
        vararg val args: Any
    ) : UiText()
    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(id, *args)
        }
    }

}

