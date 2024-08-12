package com.example.hammami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.models.User
import com.example.hammami.database.UserProfileRepository
import com.example.hammami.models.Coupon
import com.example.hammami.util.CouponManager
import com.example.hammami.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val couponManager: CouponManager
) : ViewModel() {

    val couponValues: StateFlow<List<Int>> get() = couponManager.couponValues
    val generatedCoupon: StateFlow<String?> get() = couponManager.generatedCoupon

    private val _couponGenerationResult = MutableStateFlow<Resource<Coupon>>(Resource.Unspecified())
    val couponGenerationResult: StateFlow<Resource<Coupon>> get() = _couponGenerationResult

    val user: StateFlow<Resource<User>> = userProfileRepository.authState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Loading()
    )

    fun refreshUser() {
        viewModelScope.launch {
            userProfileRepository.refreshUser()
        }
    }

    fun onCouponSelected(value: Int) {
        viewModelScope.launch {
            val currentUser = (user.value as? Resource.Success)?.data
            if (currentUser != null) {
                _couponGenerationResult.value = couponManager.generateCouponForUser(value, currentUser)
            }
        }
    }
    fun loadCoupons() {
        couponManager.loadCoupons()
    }

    fun resetGeneratedCoupon() {
        couponManager.resetGeneratedCoupon()
    }


}