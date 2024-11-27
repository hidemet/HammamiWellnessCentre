package com.example.hammami.presentation.ui.features.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.util.asUiText
import com.example.hammami.domain.model.payment.CreditCard
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.model.payment.PaymentMethod
import com.example.hammami.core.result.Result
import com.example.hammami.core.ui.UiText
import com.example.hammami.domain.model.payment.CreditCardPayment
import com.example.hammami.domain.model.payment.GooglePayPayment
import com.example.hammami.domain.model.payment.PayPalPayment
import com.example.hammami.domain.model.payment.PaymentSystem
import com.example.hammami.domain.usecase.giftcard.CreateGiftCardUseCase
import com.example.hammami.domain.usecase.payment.ApplyVoucherUseCase
import com.example.hammami.domain.usecase.payment.ProcessPaymentUseCase
import com.example.hammami.domain.usecase.user.GetUserPointsUseCase
import com.example.hammami.domain.usecase.user.getCurrentUserIdUseCase
import com.example.hammami.domain.usecase.validation.creditCard.ValidateCreditCardUseCase
import com.example.hammami.domain.usecase.validation.discount.GetDiscountUseCase
import com.example.hammami.domain.usecase.validation.discount.ValidateVoucherUseCase
import com.example.hammami.domain.usecase.validation.payment.PaymentValidationUseCase
import com.example.hammami.domain.usecase.validation.payment.PaymentValidationUseCase.PaymentValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val createGiftCardUseCase: CreateGiftCardUseCase,
    private val applyVoucherUseCase: ApplyVoucherUseCase,
    private val validateCreditCardUseCase: ValidateCreditCardUseCase,
    private val validateVoucherUseCase: ValidateVoucherUseCase,
    private val getCurrentUserIdUseCase: getCurrentUserIdUseCase,
    private val getDiscountUseCase: GetDiscountUseCase,
    private val getUserPointsUseCase: GetUserPointsUseCase,
    private val paymentValidationUseCase: PaymentValidationUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentUiState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<PaymentEvent>()
    val event = _event.asSharedFlow()

    init {
        loadUserPoints()
    }


    fun setPaymentItem(item: PaymentItem) {
        updateState {
            copy(
                paymentItem = item,
                earnedPoints = calculateEarnedPoints(item.amount)
            )
        }
    }

    private fun loadUserPoints() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            when (val result = getUserPointsUseCase()) {
                is Result.Success -> {
                    updateState { copy(totalPoints = result.data, isLoading = false) }
                }

                is Result.Error -> {
                    updateState { copy(isLoading = false) }
                    emitEvent(PaymentEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }

    fun onDiscountCodeChanged(code: String) {
        _state.update { it.copy(discountCode = code) }
    }

    fun onApplyDiscount() = viewModelScope.launch {
        val currentState = state.value
        val amount = currentState.paymentItem?.amount ?: return@launch
        val code = currentState.discountCode.trim()

        updateState { copy(isLoading = true) }

        when (val discountResult = getDiscountUseCase(code)) {
            is Result.Success -> {
                val discount = discountResult.data
                when (val validationResult = validateVoucherUseCase(discount, amount)) {
                    is Result.Success -> {
                        updateState {
                            copy(
                                appliedDiscount = discount,
                                earnedPoints = calculateEarnedPoints(amount - discount.value),
                                isLoading = false
                            )
                        }
                    }

                    is Result.Error -> {
                        updateState { copy(isLoading = false) }
                        emitEvent(PaymentEvent.ShowError(validationResult.error.asUiText()))
                    }
                }
            }

            is Result.Error -> {
                _state.update { it.copy(isLoading = false) }
                emitEvent(PaymentEvent.ShowError(discountResult.error.asUiText()))
            }
        }
    }

    fun onRemoveDiscount() {
        val originalAmount = state.value.paymentItem?.amount ?: return
        updateState {
            copy(
                appliedDiscount = null,
                discountCode = "",
                earnedPoints = calculateEarnedPoints(originalAmount)
            )
        }
    }

    private fun calculateEarnedPoints(amount: Double): Int = (amount / 10).toInt()


    fun onPaymentMethodSelected(method: PaymentMethod) {
        updateState {
            copy(
                selectedMethod = method,
                paymentSystem = when (method) {
                    PaymentMethod.CREDIT_CARD ->
                        _state.value.creditCard?.let { card -> CreditCardPayment(card) }

                    PaymentMethod.GOOGLE_PAY ->
                        GooglePayPayment("mock_gpay_token")

                    PaymentMethod.PAYPAL ->
                        PayPalPayment("mock_paypal_token")
                }
            )
        }
    }

    fun onCardDataChanged(
        number: String? = null,
        expiry: String? = null,
        cvv: String? = null
    ) {
        val currentCard = state.value.creditCard ?: CreditCard.empty()
        val updatedCard = currentCard.copy(
            number = number ?: currentCard.number,
            expiryDate = expiry ?: currentCard.expiryDate,
            cvv = cvv ?: currentCard.cvv
        )

        updateState {
            copy(
                creditCard = updatedCard,
                paymentSystem = if (selectedMethod == PaymentMethod.CREDIT_CARD)
                    CreditCardPayment(updatedCard) else paymentSystem
            )
        }
    }


    fun onConfirmPayment() = viewModelScope.launch {
        val currentState = state.value
        val paymentSystem = currentState.paymentSystem ?: return@launch
        val amount = currentState.paymentItem?.amount ?: return@launch
        val paymentItem = currentState.paymentItem ?: return@launch

        updateState { copy(isLoading = true) }

        when (val validationResult = paymentValidationUseCase(paymentSystem)) {
            is PaymentValidationResult.Valid -> processPayment(paymentSystem, paymentItem)
            is PaymentValidationResult.InvalidCreditCard -> {
                updateState {
                    copy(
                        cardErrors = PaymentUiState.CardErrors(
                            numberError = validationResult.validation.numberError?.asUiText(),
                            expiryError = validationResult.validation.expiryError?.asUiText(),
                            cvvError = validationResult.validation.cvvError?.asUiText()
                        ),
                        isLoading = false
                    )
                }
                emitEvent(PaymentEvent.ShowError(UiText.StringResource(R.string.error_invalid_credit_card)))
            }

            is PaymentValidationResult.InvalidToken -> {
                updateState { copy(isLoading = false) }
                emitEvent(PaymentEvent.ShowError(UiText.StringResource(R.string.error_invalid_payment_token)))
            }
        }
    }

    private suspend fun processPayment(paymentSystem: PaymentSystem, paymentItem: PaymentItem) {
        updateState { copy(isLoading = true) }
        when (val result = processPaymentUseCase(paymentItem.amount, paymentSystem)) {
            is Result.Success -> {
                updateState { copy(isLoading = false) }
                when (state.value.paymentItem) {
                    is PaymentItem.GiftCardPurchase -> {
                        createGiftCardUseCase(
                            value = paymentItem.amount,
                            transactionId = result.data,
                        )
                        emitEvent(
                            PaymentEvent.NavigateToGiftCardGenerated(
                                transactionId = result.data
                            )
                        )
                    }

                    is PaymentItem.ServiceBooking -> {
                        TODO()
//                        emitEvent(
//                            PaymentEvent.NavigateToBookingSummary(
//                                transactionId = result.data
//                            )
//                        )
                    }

                    null -> throw IllegalStateException("Payment item not set")
                }
            }

            is Result.Error -> {
                updateState { copy(isLoading = false) }
                emitEvent(PaymentEvent.ShowError(result.error.asUiText()))
            }
        }
    }

    private fun updateState(update: PaymentUiState.() -> PaymentUiState) {
        _state.update(update)
    }

    private suspend fun emitEvent(event: PaymentEvent) {
        _event.emit(event)
    }
}
