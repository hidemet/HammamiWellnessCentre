package com.example.hammami.core.formatter

import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import javax.inject.Inject


class CardInputFormatter @Inject constructor() {
    fun formatExpiryDate(input: String): String {
        // Inserisce lo slash automaticamente dopo i primi due numeri
        return input.take(5) // MM/YY
    }

    fun shouldAddSlash(input: String): Boolean =
        input.length == 2 && !input.contains("/")

    fun getCursorPosition(input: String): Int =
        if (shouldAddSlash(input)) 3 else input.length

}

// Implementato come extension del TextInputEditText
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
        val formatted = input.chunked(4).joinToString(" ") { it.take(4) }
        setText(formatted)
        setSelection(formatter.getCursorPosition(input))
        onValueChanged(text?.toString() ?: "")
    }
}
