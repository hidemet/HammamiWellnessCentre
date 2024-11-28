package com.example.hammami.presentation.ui.features.userProfile.coupon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.AvailableVoucher
import com.example.hammami.domain.model.DiscountVoucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.domain.usecase.GetUserVouchersUseCase
import com.example.hammami.domain.usecase.coupon.CreateCouponUseCase
import com.example.hammami.domain.usecase.coupon.GetAvailableCouponsUseCase
import com.example.hammami.domain.usecase.coupon.RedeemCouponUseCase
import com.example.hammami.domain.usecase.user.GetUserPointsUseCase
import com.example.hammami.util.ClipboardManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponViewModel @Inject constructor(
    private val redeemCouponUseCase: RedeemCouponUseCase,
    private val getUserVouchersUseCase: GetUserVouchersUseCase,
    private val getAvailableCouponsUseCase: GetAvailableCouponsUseCase,
    private val getUserPointsUseCase: GetUserPointsUseCase,
    private val clipboardManager: ClipboardManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()


    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            updateUiState { copy(isLoading = true) }

            // Carica i punti dell'utente
            val points = when (val result  = getUserPointsUseCase()) {
                is Result.Success -> result .data
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(result .error.asUiText()))
                    return@launch
                }
            }

            // Carica i coupon attivi dell'utente
            val activeCoupons = when (val result  = getUserVouchersUseCase(VoucherType.COUPON)) {
                is Result.Success -> result .data
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                    return@launch
                }
            }

            // Carica i coupon disponibili per il riscatto
            val availableCoupons = when (val result  = getAvailableCouponsUseCase()) {
                is Result.Success -> result .data
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(result .error.asUiText()))
                    return@launch
                }
            }

            updateUiState {
                copy(
                    activeCoupons = activeCoupons,
                    availableCoupons = availableCoupons,
                    userPoints = points,
                    isLoading = false
                )
            }
        }
    }

    fun onCouponSelected(coupon: AvailableVoucher) {
        viewModelScope.launch {
            if (!coupon.canBeRedeemed(uiState.value.userPoints)) {
                emitUiEvent(
                    UiEvent.ShowUserMessage(
                        UiText.StringResource(R.string.insufficient_points)
                    )
                )
                return@launch
            }
        }
    }


    fun onConfirmCouponSelection() {
        viewModelScope.launch {
            val selectedCoupon = _uiState.value.selectedCoupon ?: return@launch

            updateUiState { copy(isLoading = true) }

            when (val result = redeemCouponUseCase(
                value = selectedCoupon.value,
                requiredPoints = selectedCoupon.requiredPoints ?: 0
            )) {
                is Result.Success -> {
                    updateUiState {
                        copy(
                            isLoading = false,
                            generatedCoupon = result.data,
                            selectedCoupon = null
                        )
                    }
                    emitUiEvent(UiEvent.NavigateToCouponSuccess)
                    loadData() // Refresh della pagina
                }
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
                }
            }
        }
    }

    fun copyCouponToClipboard(code: String) {
        viewModelScope.launch {
            clipboardManager.copyToClipboard(code)
            emitUiEvent(
                UiEvent.ShowUserMessage(
                    UiText.StringResource(R.string.coupon_code_copied)
                )
            )
        }
    }


    private suspend fun emitUiEvent(event: UiEvent) {
        updateUiState { copy(isLoading = false) }
        _uiEvent.emit(event)
    }

    private fun updateUiState(update: UiState.() -> UiState) {
        _uiState.value = update(_uiState.value)
    }

    data class UiState(
        val activeCoupons: List<DiscountVoucher> = emptyList(),
        val availableCoupons: List<AvailableVoucher> = emptyList(),
        val userPoints: Int = 0,
        val isLoading: Boolean = false,
        val selectedCoupon: AvailableVoucher? = null,
        val generatedCoupon: DiscountVoucher? = null
    ) {
        val hasActiveCoupons: Boolean = activeCoupons.isNotEmpty()
        val canRedeemCoupons: Boolean = userPoints > 0
    }


    sealed class UiEvent {
        data class ShowUserMessage(val message: UiText) : UiEvent()
        data class ShowError(val message: UiText) : UiEvent()
        object NavigateToCouponSuccess : UiEvent()
    }
}