package com.example.hammami.presentation.ui.features.userProfile.giftCard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.asUiText
import com.example.hammami.core.utils.ClipboardManager
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.AvailableVoucher
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.usecase.voucher.GetAvailableVouchersUseCase
import com.example.hammami.domain.usecase.voucher.GetUserVouchersByType
import com.example.hammami.domain.usecase.voucher.GetVoucherByIdUseCase
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
    private val getUserVouchersByType: GetUserVouchersByType,
    private val getVoucherByIdUseCase: GetVoucherByIdUseCase,
    private val clipboardManager: ClipboardManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GiftCardState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

//    private val _generatedGiftCard = MutableStateFlow<Voucher?>(null)
//    val generatedGiftCard = _generatedGiftCard.asStateFlow()


    fun loadData() = viewModelScope.launch {
        Log.d("GiftCardViewModel", "Loading data")
        updateState { copy(isLoading = true) }

        // Carica le gift card disponibili
        when (val result = getAvailableVouchersUseCase(VoucherType.GIFT_CARD)) {
            is Result.Success -> {
                Log.d("GiftCardViewModel", "Loaded ${result.data.size} gift cards")
                updateState { copy(availableGiftCards = result.data) }
            }
            is Result.Error -> {
                Log.e("GiftCardViewModel", "Error loading gift cards", result.error as Throwable)
                emitEvent(UiEvent.ShowError(result.error.asUiText()))
            }

        }

        // Carica le gift card dell'utente
        when (val result = getUserVouchersByType(VoucherType.GIFT_CARD)) {
            is Result.Success -> updateState {
                copy(
                    userGiftCards = result.data,
                    isLoading = false
                )
            }
            is Result.Error -> updateState { copy(isLoading = false) }
        }
    }

    fun loadGiftCard(giftCardId: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }

            when (val result = getVoucherByIdUseCase(giftCardId)) {
                is Result.Success -> {
                    updateState { copy(generatedGiftCard = result.data, isLoading= false)  }
                   // _generatedGiftCard.value = result.data
                }
                is Result.Error -> {
                    emitEvent(UiEvent.ShowError(result.error.asUiText()))
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
        _uiState.update(update)
    }

    data class GiftCardState(
        val availableGiftCards: List<AvailableVoucher> = emptyList(),
        val userGiftCards: List<Voucher> = emptyList(),
        val selectedPaymentItem: PaymentItem.GiftCardPayment? = null,
        val generatedGiftCard: Voucher? = null,
        val isLoading: Boolean = false
    )


    sealed class UiEvent {
        data class ShowError(val message: UiText) : UiEvent()
        data class ShowMessage(val message: UiText) : UiEvent()
    }
}