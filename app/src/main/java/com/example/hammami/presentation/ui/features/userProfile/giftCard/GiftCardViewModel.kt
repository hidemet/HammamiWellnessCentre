package com.example.hammami.presentation.ui.features.userProfile.giftCard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.util.ClipboardManager
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.AvailableVoucher
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.usecase.GetAvailableVouchersUseCase
import com.example.hammami.domain.usecase.GetUserVouchersUseCase
import com.example.hammami.domain.usecase.GetVoucherByTransactionIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GiftCardViewModel @Inject constructor(
    private val getAvailableVouchersUseCase: GetAvailableVouchersUseCase,
    private val getUserVouchersUseCase: GetUserVouchersUseCase,
    private val getVoucherByTransactionIdUseCase: GetVoucherByTransactionIdUseCase,
    private val clipboardManager: ClipboardManager,
) : ViewModel() {

    private val _state = MutableStateFlow(GiftCardState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _generatedGiftCard = MutableStateFlow<Voucher?>(null)
    val generatedGiftCard = _generatedGiftCard.asStateFlow()

    init {
        loadData()
    }

    fun loadData() = viewModelScope.launch {
        updateState { copy(isLoading = true) }

        // Carica le gift card disponibili
        when (val result = getAvailableVouchersUseCase(VoucherType.GIFT_CARD)) {
            is Result.Success -> {
                updateState { copy(availableGiftCards = result.data) }
            }

            is Result.Error -> {
                updateState { copy(isLoading = false) }
            }
        }

        // Carica le gift card dell'utente
        when (val result = getUserVouchersUseCase(VoucherType.GIFT_CARD)) {
            is Result.Success -> {
                updateState {
                    copy(
                        userGiftCards = result.data,
                        isLoading = false
                    )
                }
            }

            is Result.Error -> {
                updateState { copy(isLoading = false) }
            }
        }
    }

    fun loadGiftCard(transactionId: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }

            when (val result = getVoucherByTransactionIdUseCase(transactionId)) {
                is Result.Success -> {
                    _generatedGiftCard.value = result.data
                    updateState { copy(isLoading = false) }
                }
                is Result.Error -> {
                    emitEvent(UiEvent.ShowError(result.error.asUiText()))
                    updateState { copy(isLoading = false) }
                }
            }
        }
    }

    fun copyGiftCardToClipboard(code: String) {
        clipboardManager.copyToClipboard(code)
        viewModelScope.launch {
            emitEvent(UiEvent.ShowMessage(UiText.StringResource(R.string.gift_card_code_copied)))
        }
    }

    private suspend fun emitEvent(event: UiEvent) {
        updateState { copy(isLoading = false) }
        _uiEvent.emit(event)
    }

    private fun updateState(update: GiftCardState.() -> GiftCardState) {
        _state.update(update)
    }

    data class GiftCardState(
        val availableGiftCards: List<AvailableVoucher> = emptyList(),
        val userGiftCards: List<Voucher> = emptyList(),
        val selectedPaymentItem: PaymentItem.GiftCardPayment? = null,
        val generatedGiftCard: Voucher? = null,
        val isLoading: Boolean = false
    ) {
        val hasGiftCards: Boolean get() = userGiftCards.isNotEmpty()
        val hasAvailableGiftCards: Boolean get() = availableGiftCards.isNotEmpty()
    }


    sealed class UiEvent {
        data class ShowError(val message: UiText) : UiEvent()
        data class ShowMessage(val message: UiText) : UiEvent()
        object NavigateToPayment : UiEvent()
        // object GiftCardPurchaseSuccess : UiEvent()
    }
}