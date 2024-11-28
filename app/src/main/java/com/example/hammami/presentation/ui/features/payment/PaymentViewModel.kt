package com.example.hammami.presentation.ui.features.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.util.KarmaPointsCalculator
import com.example.hammami.core.util.asUiText
import com.example.hammami.domain.model.payment.CreditCard
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.model.payment.PaymentMethod
import com.example.hammami.core.result.Result
import com.example.hammami.core.ui.UiText
import com.example.hammami.domain.model.DiscountVoucher
import com.example.hammami.domain.model.payment.CreditCardPayment
import com.example.hammami.domain.model.payment.PaymentSystem
import com.example.hammami.domain.usecase.GetDiscountVoucherUseCase
import com.example.hammami.domain.usecase.ValidateVoucherUseCase
import com.example.hammami.domain.usecase.payment.ProcessPaymentUseCase
import com.example.hammami.domain.usecase.user.GetUserPointsUseCase
import com.example.hammami.domain.usecase.validation.creditCard.ValidateCreditCardUseCase
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
    private val validateCreditCardUseCase: ValidateCreditCardUseCase,
    private val validateVoucherUseCase: ValidateVoucherUseCase,
    private val getDiscountVoucherUseCase: GetDiscountVoucherUseCase,
    private val getUserPointsUseCase: GetUserPointsUseCase,
    private val paymentValidationUseCase: PaymentValidationUseCase,
    private val karmaPointsCalculator: KarmaPointsCalculator

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
                finalAmount = item.amount,
                earnedPoints = karmaPointsCalculator.calculatePoints(item.amount, item)
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
                    emitEvent(PaymentEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }

    fun onDiscountCodeChanged(code: String) {
        _state.update { it.copy(discountCode = code) }
    }

    fun onApplyVoucher() = viewModelScope.launch {
        val currentState = state.value
        val amount = currentState.paymentItem.amount
        val code = currentState.discountCode

        updateState { copy(isLoading = true) }

        when (val voucherResult = getDiscountVoucherUseCase(code)) {
            is Result.Success -> {
                val voucher = voucherResult.data
                when (val validationResult = validateVoucherUseCase(voucher, amount)) {
                    is Result.Success -> {
                        val newAmount = amount - voucher.value
                        updateState {
                            copy(
                                appliedVoucher = voucher,
                                finalAmount = newAmount,
                                discountCode = "",
                                earnedPoints = karmaPointsCalculator.calculatePoints(newAmount, paymentItem),
                                isLoading = false
                            )
                        }
                    }

                    is Result.Error -> {
                        updateState {
                            copy(discountError = validationResult.error.asUiText(),)
                        }
                        emitEvent(PaymentEvent.ShowError(validationResult.error.asUiText()))
                    }
                }
            }
            is Result.Error -> {
                emitEvent(PaymentEvent.ShowError(voucherResult.error.asUiText()))
            }
        }
    }

    fun onRemoveVoucher() {
        val amount = state.value.paymentItem.amount
        updateState {
            copy(
                appliedVoucher = null,
                finalAmount = amount,
                discountCode = "",
                discountError = null,
                earnedPoints = karmaPointsCalculator.calculatePoints(amount, paymentItem)
            )
        }
    }

    fun onPaymentMethodSelect(method: PaymentMethod) {
        updateState {
            copy(
                selectedMethod = method,
                cardValidationErrors = null,
                isPaymentEnabled = method != PaymentMethod.CREDIT_CARD
            )
        }
    }

    fun onCardDataChanged(
        number: String? = null,
        expiry: String? = null,
        cvv: String? = null
    ) {
        if (state.value.selectedMethod != PaymentMethod.CREDIT_CARD) return

        val currentCard = state.value.creditCard ?: CreditCard.empty()
        val updatedCard = currentCard.copy(
            number = number ?: currentCard.number,
            expiryDate = expiry ?: currentCard.expiryDate,
            cvv = cvv ?: currentCard.cvv
        )

        viewModelScope.launch {
            val validationResult = validateCreditCardUseCase(updatedCard)
            if (validationResult.isValid) {
                updateState {
                    copy(
                        creditCard = updatedCard,
                        cardValidationErrors = null,
                        isPaymentEnabled = true,
                        paymentSystem = CreditCardPayment(updatedCard),
                    )
                }
            } else {
                updateState {
                    copy(
                        creditCard = updatedCard,
                        paymentSystem = null,
                        isPaymentEnabled = false,
                        cardValidationErrors = PaymentUiState.CardValidationErrors(
                            numberError = validationResult.numberError?.asUiText(),
                            expiryError = validationResult.expiryError?.asUiText(),
                            cvvError = validationResult.cvvError?.asUiText()
                        )
                    )
                }
            }
        }
    }


    fun onConfirmPayment() = viewModelScope.launch {
        val currentState = state.value
        val paymentSystem = currentState.paymentSystem ?: return@launch
        val amount = currentState.paymentItem.amount
        val paymentItem = currentState.paymentItem

        updateState { copy(isLoading = true) }

        when (val validationResult = paymentValidationUseCase(paymentSystem)) {
            is PaymentValidationResult.Valid -> processPayment(
                amount,
                paymentSystem,
                currentState.appliedVoucher
            )

            is PaymentValidationResult.InvalidCreditCard -> {
                updateState {
                    copy(
                        cardValidationErrors = PaymentUiState.CardValidationErrors(
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

    private suspend fun processPayment(
        amount: Double,
        paymentSystem: PaymentSystem,
        appliedVoucher: DiscountVoucher?
    ) {
        when (val result = processPaymentUseCase(paymentSystem, amount, appliedVoucher)) {
            is Result.Success -> {
                updateState { copy(isLoading = false) }
                when (state.value.paymentItem) {
                    is PaymentItem.GiftCardPurchase -> {
                        emitEvent(PaymentEvent.NavigateToGiftCardGenerated(transactionId = result.data))
                    }

                    is PaymentItem.ServiceBooking -> {
                        TODO()
//                        emitEvent(
//                            PaymentEvent.NavigateToBookingSummary(
//                                transactionId = result.data
//                            )
//                        )
                    }
                }
            }

            is Result.Error -> {
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
