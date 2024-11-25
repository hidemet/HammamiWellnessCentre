package com.example.hammami.presentation.ui.features.userProfile.giftCard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.domain.model.giftCard.GiftCard
import com.example.hammami.domain.usecase.giftcard.GetAvailableGiftCardsUseCase
import com.example.hammami.domain.usecase.giftcard.GetUserGiftCardsUseCase
import com.example.hammami.util.ClipboardManager
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.giftCard.AvailableGiftCard
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.usecase.giftcard.GetGiftCardByTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GiftCardViewModel @Inject constructor(
    private val getGiftCardByTransactionUseCase: GetGiftCardByTransactionUseCase,
    private val getAvailableGiftCardsUseCase: GetAvailableGiftCardsUseCase,
private val getUserGiftCardsUseCase: GetUserGiftCardsUseCase,
    private val clipboardManager: ClipboardManager,
) : ViewModel() {

    private val _state = MutableStateFlow(GiftCardState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadData()
    }

    fun loadData() = viewModelScope.launch {
        Log.d("GiftCardVM", "Starting to load data")
        updateState { copy(isLoading = true) }

        try {
            val availableCards = getAvailableGiftCards()
            Log.d("GiftCardVM", "Available cards loaded: $availableCards")
            val userCards = getUserGiftCards()
            Log.d("GiftCardVM", "User cards loaded: $userCards")

            updateState {
                copy(
                    isLoading = false,
                    availableGiftCards = availableCards,
                    userGiftCards = userCards
                )
            }
        } catch (e: Exception) {
            Log.e("GiftCardVM", "Error loading data", e)
            emitUiEvent(UiEvent.ShowError(UiText.DynamicString(e.message ?: "Unknown error")))
            updateState { copy(isLoading = false) }
        }
    }

    fun loadGiftCard(transactionId: String) {
        viewModelScope.launch {
           updateState{ copy(isLoading = true) }

            when (val result = getGiftCardByTransactionUseCase(transactionId)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            generatedGiftCard = result.data
                            )
                    }
                }
                is Result.Error -> {
                    updateState{ copy(isLoading = false) }
                    emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }

    fun copyGiftCardToClipboard(code: String) {
        clipboardManager.copyToClipboard(code)
        viewModelScope.launch {
            emitUiEvent(
                UiEvent.ShowMessage(
                    UiText.StringResource(R.string.gift_card_code_copied)
                )
            )
        }
    }

    private suspend fun getAvailableGiftCards(): List<AvailableGiftCard> {
        return when (val result = getAvailableGiftCardsUseCase()) {
            is Result.Success -> result.data
            is Result.Error -> {
                emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                emptyList()
            }
        }
    }

    private suspend fun getUserGiftCards(): List<GiftCard> {
        return when (val result = getUserGiftCardsUseCase()) {
            is Result.Success -> result.data
            is Result.Error -> {
                emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                emptyList()
            }
        }
    }

    data class GiftCardState(
        val availableGiftCards: List<AvailableGiftCard> = emptyList(),
        val userGiftCards: List<GiftCard> = emptyList(),
        val generatedGiftCard: GiftCard? = null,
        val selectedPaymentItem: PaymentItem.GiftCardPurchase? = null,
        val isLoading: Boolean = false
    ) {
        val hasGiftCards: Boolean get() = userGiftCards.isNotEmpty()
        val hasAvailableGiftCards: Boolean get() = availableGiftCards.isNotEmpty()
    }


    sealed class UiEvent {
        data class ShowError(val message: UiText) : UiEvent()
        data class ShowMessage(val message: UiText) : UiEvent()
        object GiftCardPurchaseSuccess : UiEvent()
    }

    private suspend fun emitUiEvent(event: UiEvent) {
        _uiEvent.emit(event)
    }

    private fun updateState(update: GiftCardState.() -> GiftCardState) {
        _state.update(update)
    }
}