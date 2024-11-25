package com.example.hammami.presentation.ui.features.userProfile.coupon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.domain.model.coupon.AvailableCoupon
import com.example.hammami.domain.model.coupon.Coupon
import com.example.hammami.core.result.Result
import com.example.hammami.domain.usecase.coupon.GenerateCouponUseCase
import com.example.hammami.domain.usecase.coupon.GetActiveCouponsUseCase
import com.example.hammami.domain.usecase.coupon.GetAvailableCouponsUseCase
import com.example.hammami.domain.usecase.user.ObserveUserStateUseCase
import com.example.hammami.domain.usecase.user.RefreshUserStateUseCase
import com.example.hammami.util.ClipboardManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponViewModel @Inject constructor(
    private val getAvailableCouponsUseCase: GetAvailableCouponsUseCase,
    private val getActiveCouponsUseCase: GetActiveCouponsUseCase,
    private val generateCouponUseCase: GenerateCouponUseCase,
    private val clipboardManager: ClipboardManager,
    private val observeUserStateUseCase: ObserveUserStateUseCase,
    private val refreshUserStateUseCase: RefreshUserStateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CouponState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _selectedCoupon = MutableStateFlow<AvailableCoupon?>(null)
    val selectedCoupon = _selectedCoupon.asStateFlow()


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

    fun selectCoupon(coupon: AvailableCoupon) {
        _selectedCoupon.value = coupon
    }

    private fun observeUserState() {
        viewModelScope.launch {
            observeUserStateUseCase()
                .collect { result ->
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
            updateState { copy(isLoading = true) }
            refreshUserStateUseCase()
            loadCoupons()
            updateState { copy(isLoading = false) }
        }
    }

    private suspend fun loadCoupons() {
        when (val activeResult = getActiveCouponsUseCase()) {
            is Result.Success -> {
                updateState { copy(activeCoupons = activeResult.data) }
            }

            is Result.Error -> {
                emitUiEvent(UiEvent.ShowUserMessage(activeResult.error.asUiText()))
            }
        }

        when (val availableResult = getAvailableCouponsUseCase()) {
            is Result.Success -> updateState { copy(availableCoupons = availableResult.data) }
            is Result.Error -> emitUiEvent(UiEvent.ShowUserMessage(availableResult.error.asUiText()))
        }
    }

    fun onCouponSelected(value: Double) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = generateCouponUseCase(value)) {
                is Result.Success -> {
                    handleCouponGeneration(result.data)
                }
                is Result.Error -> {
                    _uiEvent.emit(UiEvent.ShowUserMessage(result.error.asUiText()))
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun copyCouponToClipboard(code: String) {
        viewModelScope.launch {
            clipboardManager.copyToClipboard(code)
            _uiEvent.emit(UiEvent.ShowUserMessage(
                UiText.StringResource(R.string.coupon_code_copied)
            ))
        }
    }


    private fun handleCouponGeneration(coupon: Coupon) {
        viewModelScope.launch {
            _state.update { it.copy(generatedCoupon = coupon) }
            _uiEvent.emit(UiEvent.NavigateToCouponSuccess)
        }
    }


    private suspend fun emitUiEvent(event: UiEvent) {
        _uiEvent.emit(event)
    }

    private fun updateState(update: CouponState.() -> CouponState) {
        _state.value = update(_state.value)
    }

    data class CouponState(
        val userPoints: Int = 0,
        val availableCoupons: List<AvailableCoupon> = emptyList(),
        val activeCoupons: List<Coupon> = emptyList(),
        val generatedCoupon: Coupon? = null,
        val isLoading: Boolean = false
    ) {
        val canRedeemCoupons: Boolean get() = userPoints >= 0
        val hasActiveCoupons: Boolean get() = activeCoupons.isNotEmpty()

    }

    sealed class UiEvent {
        data class ShowUserMessage(val message: UiText) : UiEvent()
        object NavigateToCouponSuccess : UiEvent()
    }
}