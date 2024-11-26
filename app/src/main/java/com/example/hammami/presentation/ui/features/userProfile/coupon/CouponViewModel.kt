package com.example.hammami.presentation.ui.features.userProfile.coupon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.domain.model.coupon.AvailableVoucher
import com.example.hammami.core.result.Result
import com.example.hammami.domain.model.AvailableVoucher
import com.example.hammami.domain.model.DiscountVoucher
import com.example.hammami.domain.usecase.GetAvailableVouchersUseCase
import com.example.hammami.domain.usecase.coupon.CreateCouponUseCase
import com.example.hammami.domain.usecase.coupon.GetUserCouponsUseCase
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
    private val getUserCouponsUseCase: GetUserCouponsUseCase,
    private val getAvailableVouchersUseCase: GetAvailableVouchersUseCase,
    private val createCouponUseCase: CreateCouponUseCase,
    private val getUserPointsUseCase: GetUserPointsUseCase,
    private val clipboardManager: ClipboardManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()


    init {
        // Inizia osservando i dati utente
        observeUserState()
        // Carica i dati iniziali
        loadData()
    }

    fun resetCouponSelection() {
        _selectedCoupon.value = null
        updateState { copy(generatedCoupon = null) }
    }

    fun selectCoupon(coupon: com.example.hammami.domain.model.coupon.AvailableVoucher) {
        _selectedCoupon.value = coupon
    }

    private fun observeUserState() {
        viewModelScope.launch {
            observeUserStateUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        updateState {
                            copy(userPoints = result.data?.points ?: 0)
                        }
                    }

                    is Result.Error -> {
                        updateState { copy(userPoints = 0) }
                        emitUiEvent(UiEvent.ShowUserMessage(result.error.asUiText()))
                    }
                }
            }
        }
    }


    fun loadData() {
        viewModelScope.launch {
            updateUiState { copy(isLoading = true) }

            // Carica i punti dell'utente
            val points = when (val pointsResult = getUserPointsUseCase()) {
                is Result.Success -> pointsResult.data
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(pointsResult.error.asUiText()))
                    return@launch
                }
            }

            // Carica i coupon attivi dell'utente
            val activeCoupons = when (val couponsResult = getUserCouponsUseCase()) {
                is Result.Success -> couponsResult.data
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(couponsResult.error.asUiText()))
                    return@launch
                }
            }

            // Carica i coupon disponibili per il riscatto
            val availableCoupons = when (val availableResult = getAvailableVouchersUseCase()) {
                is Result.Success -> availableResult.data
                is Result.Error -> {
                    emitUiEvent(UiEvent.ShowError(availableResult.error.asUiText()))
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

    fun onCouponSelected(coupon: com.example.hammami.domain.model.coupon.AvailableVoucher) {
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


    fun onConfirmCouponSelection() = viewModelScope.launch {
        val selectedCoupon = _uiState.value.selectedCoupon ?: return@launch


        when (val result = createCouponUseCase(value = selectedCoupon.value)) {
            is Result.Success -> {
                updateUiState {
                    copy(
                        generatedCoupon = result.data, selectedCoupon = null
                    )
                }
                emitUiEvent(UiEvent.NavigateToCouponSuccess)
            }

            is Result.Error -> emitUiEvent(UiEvent.ShowError(result.error.asUiText()))
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