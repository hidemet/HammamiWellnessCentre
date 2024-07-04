import com.google.android.material.textfield.TextInputLayout

// Classe di utilità per la convalida
object ValidationUtil {
    fun validateAndReturnField(
        field: TextInputLayout,
        emptyError: String,
        invalidError: String? = null,
        validation: ((String) -> Boolean)? = null
    ): String? {
        val text = field.editText?.text.toString()
        var error: String? = null
        when {
            text.isBlank() -> error = emptyError
            validation != null && !validation(text) -> error = invalidError
        }
        field.error = error
        return text.takeIf { error == null }
    }

    fun isValidInput(
        field: TextInputLayout,
        emptyError: String,
        invalidError: String? = null,
        validation: ((String) -> Boolean)? = null
        ): Boolean {
            val text = field.editText?.text.toString()
            var error: String? = null
            when {
                text.isBlank() -> error = emptyError
                validation != null && !validation(text) -> error = invalidError
            }
            field.error = error
            return error != null
        }
    }
