package com.example.hammami.presentation.ui.features.payment

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.core.utils.KarmaPointsCalculator
import com.example.hammami.core.utils.asUiText
import com.example.hammami.domain.model.payment.CreditCard
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.model.payment.PaymentMethod
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.payment.CreditCardPayment
import com.example.hammami.domain.model.payment.GooglePayPayment
import com.example.hammami.domain.model.payment.PayPalPayment
import com.example.hammami.domain.model.payment.PaymentSystem
import com.example.hammami.domain.usecase.payment.ProcessPaymentUseCase
import com.example.hammami.domain.usecase.voucher.GetVoucherByCodeUseCase
import com.example.hammami.domain.usecase.voucher.ValidateVoucherUseCase
import com.example.hammami.domain.usecase.user.GetUserPointsUseCase
import com.example.hammami.domain.usecase.validation.creditCard.ValidateCreditCardUseCase
import com.example.hammami.presentation.ui.features.NotificationReceiverFragment
import com.example.hammami.presentation.ui.features.NotificationReceiverFragment.Companion.REQUEST_CODE_POST_NOTIFICATIONS
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone


@AssistedFactory
interface PaymentViewModelFactory {
    fun create(paymentItem: PaymentItem): PaymentViewModel
}

class PaymentViewModel @AssistedInject constructor(
    @Assisted private val paymentItem: PaymentItem,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val validateCreditCardUseCase: ValidateCreditCardUseCase,
    private val validateVoucherUseCase: ValidateVoucherUseCase,
    private val getDiscountVoucherUseCase: GetVoucherByCodeUseCase,
    private val getUserPointsUseCase: GetUserPointsUseCase,
    private val karmaPointsCalculator: KarmaPointsCalculator,
) : ViewModel() {

    private val _state = MutableStateFlow(
        PaymentUiState(
            paymentItem = paymentItem,
            itemPrice = paymentItem.price,
            finalAmount = paymentItem.price,
            currentKarmaPoints = 0,
            earnedKarmaPoints = karmaPointsCalculator.calculatePoints(
                paymentItem.price)
        )
    )
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<PaymentEvent>()
    val event = _event.asSharedFlow()

    init {
        Log.e("PaymentViewModel", "paymentItem.id: $paymentItem.id")
        loadUserPoints()
    }

    private fun loadUserPoints() = viewModelScope.launch {
        when (val result = getUserPointsUseCase()) {
            is Result.Success -> updateState { copy(currentKarmaPoints = result.data) }
            is Result.Error -> emitEvent(PaymentEvent.ShowError(result.error.asUiText()))
        }
    }


    fun onDiscountCodeChanged(code: String) = _state.update { it.copy(discountCode = code) }


    fun onApplyVoucher() = viewModelScope.launch {
        val itemPrice = state.value.paymentItem.price
        val discountCode = state.value.discountCode.trim()
        updateState { copy(isLoading = true) }
        when (val voucherResult = getDiscountVoucherUseCase(discountCode)) {
            is Result.Success -> {
                val voucher = voucherResult.data
                when (val validationResult = validateVoucherUseCase(voucher, itemPrice)) {
                    is Result.Success -> {
                        val newAmount = itemPrice - voucher.value
                        updateState {
                            copy(
                                appliedVoucher = voucher,
                                discountValue = voucher.value,
                                finalAmount = newAmount,
                                earnedKarmaPoints = karmaPointsCalculator.calculatePoints(
                                    newAmount),
                                discountCode = "",
                                discountError = null,
                                isLoading = false
                            )
                        }
                    }

                    is Result.Error -> {
                        updateState {
                            copy(
                                discountError = validationResult.error.asUiText(),
                                discountCode = "",
                            )
                        }
                        emitEvent(PaymentEvent.ShowError(validationResult.error.asUiText()))
                    }
                }
            }

            is Result.Error -> {
                updateState {
                    copy(
                        discountError = voucherResult.error.asUiText(),
                        discountCode = "",
                    )
                }
                emitEvent(PaymentEvent.ShowError(voucherResult.error.asUiText()))
            }
        }
    }

    fun onRemoveVoucher() {
        val itemPrice = state.value.paymentItem.price
        updateState {
            copy(
                appliedVoucher = null,
                discountValue = null,
                finalAmount = itemPrice,
                earnedKarmaPoints = karmaPointsCalculator.calculatePoints(itemPrice),
                discountCode = "",
                discountError = null
            )
        }
    }

    fun onPaymentMethodSelect(method: PaymentMethod) = viewModelScope.launch {
        updateState {
            copy(
                selectedMethod = method,
                isPaymentEnabled = if (method == PaymentMethod.CREDIT_CARD) {
                    isCreditCardDataValid(creditCard)
                } else {
                    true
                }
            )
        }
    }

    private fun isCreditCardDataValid(creditCard: CreditCard?): Boolean {
        return creditCard?.number?.isNotBlank() == true &&
                creditCard.expiryDate.isNotBlank() &&
                creditCard.cvv.isNotBlank()
    }


    fun onCardDataChanged(number: String? = null, expiry: String? = null, cvv: String? = null) {
        if (state.value.selectedMethod != PaymentMethod.CREDIT_CARD) return
        val currentCard = state.value.creditCard ?: CreditCard("", "", "")
        val updatedCard = currentCard.copy(
            number = number ?: currentCard.number,
            expiryDate = expiry ?: currentCard.expiryDate,
            cvv = cvv ?: currentCard.cvv
        )
        updateState {
            copy(
                creditCard = updatedCard,
                isPaymentEnabled = isCreditCardDataValid(updatedCard)
            )
        }
    }

    fun onConfirmPayment() = viewModelScope.launch {
        updateState { copy(isLoading = true) }
        val currentState = state.value
        when (currentState.selectedMethod) {
            PaymentMethod.CREDIT_CARD -> processCreditCardPayment(currentState)
            PaymentMethod.PAYPAL -> processPayment(PayPalPayment("dummy_token"), currentState)
            PaymentMethod.GOOGLE_PAY -> processPayment(
                GooglePayPayment("dummy_token"),
                currentState
            )
        }
    }

    private suspend fun processCreditCardPayment(currentState: PaymentUiState) {
        val creditCard = currentState.creditCard ?: return
        val validationResult = validateCreditCardUseCase(creditCard)
        val cardValidationErrors = PaymentUiState.CardValidationErrors(
            numberError = validationResult.numberError?.asUiText(),
            expiryError = validationResult.expiryError?.asUiText(),
            cvvError = validationResult.cvvError?.asUiText()
        )
        if (cardValidationErrors.isCardValid()) {
            processPayment(CreditCardPayment(creditCard), currentState)
        } else {
            updateState {
                copy(
                    cardValidationErrors = cardValidationErrors,
                    isLoading = false
                )
            }
        }
    }


    private suspend fun processPayment(paymentSystem: PaymentSystem, currentState: PaymentUiState) {
        updateState { copy(isLoading = true) }
        when (val paymentResult = processPaymentUseCase(
            paymentSystem,
            currentState.paymentItem,
            currentState.finalAmount,
            currentState.appliedVoucher
        )) {
            is Result.Success -> handlePaymentSuccess(paymentResult.data)
            is Result.Error -> emitEvent(PaymentEvent.ShowError(paymentResult.error.asUiText()))
        }
    }

    private suspend fun handlePaymentSuccess(itemId: String) {
        Log.d("PaymentViewModel", "Payment successful with transaction ID: $itemId")
        when (paymentItem) {
            is PaymentItem.GiftCardPayment -> emitEvent(
                PaymentEvent.NavigateToGiftCardGenerated(
                    itemId
                )
            )

            is PaymentItem.ServiceBookingPayment -> {
                emitEvent(PaymentEvent.NavigateToBookingSummary(itemId))
            }
        }
        updateState { copy(isLoading = false) }
    }

    /*
    private suspend fun handlePaymentSuccess(transactionId: String, paymentItem: PaymentItem) {
        Log.d("PaymentViewModel", "Payment successful with transaction ID: $transactionId")
        when (paymentItem) {
            is PaymentItem.GiftCardPayment -> emitEvent(PaymentEvent.NavigateToGiftCardGenerated(transactionId))
            is PaymentItem.ServiceBookingPayment -> {
                val booking = paymentItem.booking
                val bookingDateMillis = booking.dateMillis
                val notificationTime = bookingDateMillis - 24 * 60 * 60 * 1000 // 24 hours before the booking time

                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        REQUEST_CODE_POST_NOTIFICATIONS
                    )
                    return
                }
                scheduleNotification(context, notificationTime, booking.id)
                emitEvent(PaymentEvent.NavigateToBookingSummary(paymentItem.bookingId))
            }
        }
        updateState { copy(isLoading = false) }
    }

     */

    private fun updateState(update: PaymentUiState.() -> PaymentUiState) {
        _state.update(update)
    }

    private suspend fun emitEvent(event: PaymentEvent) {
        updateState { copy(isLoading = false) }
        _event.emit(event)

    }
}
