package com.example.hammami.presentation.ui.features.userProfile.coupon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.R
import com.example.hammami.databinding.DialogCouponConfirmationBinding
import com.example.hammami.databinding.FragmentCouponSelectionBinding
import com.example.hammami.domain.model.AvailableVoucher
import com.example.hammami.presentation.ui.adapters.AvailableCouponAdapter
import com.example.hammami.presentation.ui.features.BaseFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CouponSelectionFragment : BaseFragment() {
    private var _binding: FragmentCouponSelectionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CouponViewModel by activityViewModels()
    private lateinit var couponAdapter: AvailableCouponAdapter


    private var confirmationDialog: MaterialAlertDialogBuilder? = null
    private var confirmationDialogView: DialogCouponConfirmationBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCouponSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        setupToolbar()
        setupAvailableCouponsRecyclerView()
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvent.collect { event ->
                handleUiEvent(event)
            }
        }
    }

    private fun handleUiEvent(event: CouponViewModel.UiEvent) {
        when (event) {
            is CouponViewModel.UiEvent.ShowUserMessage -> {
                showSnackbar(event.message)
            }

            CouponViewModel.UiEvent.NavigateToCouponSuccess -> {
                findNavController().navigate(R.id.action_couponSelectionFragment_to_couponSuccessFragment)
            }

            else -> Unit
        }
    }

    private fun setupToolbar() = with(binding.topAppBar) {
        setNavigationOnClickListener { onBackClick() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.tvPoints.text = getString(R.string.points_format, state.userPoints)
            }
        }
    }

    private fun setupAvailableCouponsRecyclerView() {
        couponAdapter = AvailableCouponAdapter { availableCoupon ->
         //   viewModel.onCouponSelected(availableCoupon)
            showConfirmationDialog(availableCoupon)
        }

        binding.rvAvailableCoupons.apply {
            adapter = couponAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                couponAdapter.submitList(state.availableCoupons)
            }
        }


    }

    private fun showConfirmationDialog(coupon: AvailableVoucher) {
        val dialogBinding = DialogCouponConfirmationBinding.inflate(layoutInflater)

        dialogBinding.confirmationText.text = getString(
            R.string.confirm_redemption_message,
            coupon.value,
            coupon.requiredPoints
        )
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                viewModel.onConfirmCouponSelection(coupon)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        confirmationDialog = null
        confirmationDialogView = null
        _binding = null
    }
}