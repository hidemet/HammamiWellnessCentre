package com.example.hammami.domain.model.payment

data class CreditCard private constructor(
    var number: String,
    var expiryDate: String,
    var cvv: String
) {
    fun isValid(): Boolean =
        isNumberValid() &&
                isExpiryDateValid() &&
                isCvvValid()

    private fun isNumberValid(): Boolean =
        number.length == CARD_NUMBER_LENGTH &&
                number.all { it.isDigit() } &&
                isLuhnValid()

    private fun isExpiryDateValid(): Boolean {
        if (!expiryDate.matches("""^\d{2}/\d{2}$""".toRegex())) return false

        val (month, year) = expiryDate.split("/").map { it.toInt() }
        val currentYear = java.time.Year.now().value % 100
        val currentMonth = java.time.MonthDay.now().monthValue

        return when {
            month !in 1..12 -> false
            year < currentYear -> false
            year > currentYear -> true
            else -> month >= currentMonth
        }
    }

    private fun isCvvValid(): Boolean =
        cvv.length == CVV_LENGTH && cvv.all { it.isDigit() }

    private fun isLuhnValid(): Boolean {
        var sum = 0
        var alternate = false

        for (i in number.length - 1 downTo 0) {
            var n = number[i].toString().toInt()
            if (alternate) {
                n *= 2
                if (n > 9) n = (n % 10) + 1
            }
            sum += n
            alternate = !alternate
        }

        return (sum % 10 == 0)
    }

    companion object {
        const val CARD_NUMBER_LENGTH = 16
        const val CVV_LENGTH = 3
        const val EXPIRY_DATE_LENGTH = 5

        fun create(number: String, expiryDate: String, cvv: String) = CreditCard(
            number = number.trim().replace("\\s".toRegex(), ""),
            expiryDate = expiryDate.trim(),
            cvv = cvv.trim()
        )

        fun empty() = CreditCard("", "", "")
    }
}