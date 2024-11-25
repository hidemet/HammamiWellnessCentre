sealed class RegistrationFormEvent {
    data class Submit(val currentStep: RegistrationStep, val formData: Map<String, String>) : RegistrationFormEvent()
}

enum class RegistrationStep {
    PERSONAL_INFO,
    HEALTH_INFO,
    CONTACT_INFO,
    CREDENTIALS,
    FRIEND_CODE
}