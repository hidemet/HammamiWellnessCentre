package com.example.hammami.presentation.ui.features.userProfile.coupon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hammami.R
import com.example.hammami.databinding.FragmentCouponSuccessBinding
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CouponSuccessFragment : BaseFragment() {
    private var _binding: FragmentCouponSuccessBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CouponViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCouponSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        setupTopBar()
        setupCouponDetails()
        setupButtons()
    }

    private fun setupTopBar() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack(R.id.couponFragment, false)
        }
    }

    private fun setupCouponDetails() {
        viewModel.uiState.value.generatedCoupon?.let { coupon ->
            // Access views in the included layout using binding.couponCard
            with(binding.couponCard) {
                // Set coupon value
                voucherValue.text = getString(
                    R.string.coupon_value_format,
                    coupon.value
                )
                // Set coupon code in TextInputEditText
                voucherCode.setText(coupon.code)
                // Set expiration date
                voucherExpiry.text = getString(
                    R.string.coupon_expiration_format,
                    coupon.expirationDate
                )
                // Setup click listener for copy button
                voucherCodeLayout.setEndIconOnClickListener {
                    viewModel.copyCouponToClipboard(coupon.code)
                }
            }
        }
    }

    private fun setupButtons() = with(binding) {
        // Bottone per tornare alla lista dei coupon
        buttonBackToCoupons.setOnClickListener {
            findNavController().popBackStack(R.id.couponFragment, false)
        }

        // Bottone per tornare alla home
        buttonBackToHome.setOnClickListener {
            findNavController().navigate(R.id.action_global_home)
        }
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is CouponViewModel.UiEvent.ShowUserMessage -> {
                        showSnackbar(event.message)
                    }

                    else -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}