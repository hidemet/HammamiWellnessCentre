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
import java.text.SimpleDateFormat
import java.util.Locale

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
            with(binding.couponCard) {
                icon.setImageResource(R.drawable.ic_coupon)
                titleText.text = getString(
                    R.string.coupon,
                )
                voucherValue.text = getString(
                    R.string.coupon_value_format,
                    coupon.value
                )
                voucherCode.setText(coupon.code)

                voucherExpiry.text = getString(
                    R.string.expires_on,
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(coupon.expirationDate.toDate())
                )
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
            findNavController().navigate(R.id.action_couponSuccessFragment_to_homeFragment)
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