package com.example.hammami.core.formatter

import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import javax.inject.Inject


class CardInputFormatter @Inject constructor() {
    fun formatExpiryDate(input: String): String {
        return input.take(5) // MM/YY
    }

    fun shouldAddSlash(input: String): Boolean =
        input.length == 2 && !input.contains("/")

    fun getCursorPosition(input: String): Int =
        if (shouldAddSlash(input)) 3 else input.length

}

fun TextInputEditText.setupExpiryDateFormatting(
    formatter: CardInputFormatter,
    onValueChanged: (String) -> Unit
) {
    doAfterTextChanged { text ->
        val input = text?.toString() ?: ""
        if (formatter.shouldAddSlash(input)) {
            setText("$input/")
            setSelection(formatter.getCursorPosition(input))
        }
        onValueChanged(text?.toString() ?: "")
    }
}

fun TextInputEditText.setupCardNumberFormatting(
    formatter: CardInputFormatter,
    onValueChanged: (String) -> Unit
) {
    doAfterTextChanged { text ->
        val input = text?.toString() ?: ""
        val formatted = input.replace("\\s".toRegex(), "").chunked(4).joinToString(" ")
        if (text?.toString() != formatted) {
            setText(formatted)
            setSelection(formatted.length)
        }
        onValueChanged(input.replace("\\s".toRegex(), ""))
    }
}
