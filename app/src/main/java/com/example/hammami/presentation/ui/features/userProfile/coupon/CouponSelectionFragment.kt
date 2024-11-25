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
import com.example.hammami.domain.model.coupon.AvailableCoupon
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
        }
    }

    private fun setupToolbar() = with(binding.topAppBar) {
        setNavigationOnClickListener { onBackClick() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.tvPoints.text = getString(R.string.points_format, state.userPoints)
            }
        }
    }

    private fun setupAvailableCouponsRecyclerView() {
        val adapter = AvailableCouponAdapter { availableCoupon ->
            viewModel.selectCoupon(availableCoupon)
            showConfirmationDialog()
        }

        binding.rvAvailableCoupons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                adapter.submitList(state.availableCoupons)
            }
        }
    }

    private fun showConfirmationDialog() {
        confirmationDialogView = DialogCouponConfirmationBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(confirmationDialogView?.root)
            .setCancelable(false)
            .create()

        setupDialogContent()
        setupDialogButtons(dialog)
        observeDialogState(dialog)

        dialog.show()
    }

    private fun setupDialogContent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedCoupon.collect { coupon ->
                coupon?.let { updateDialogContent(it) }
            }
        }
    }

    private fun updateDialogContent(coupon: AvailableCoupon) {
        confirmationDialogView?.apply {
            confirmationText.text = getString(
                R.string.confirm_redemption_message,
                coupon.value,
                coupon.requiredPoints
            )
        }
    }

    private fun setupDialogButtons(dialog: androidx.appcompat.app.AlertDialog) {
        confirmationDialogView?.apply {
            buttonCancel.setOnClickListener {
                dialog.dismiss()
                viewModel.resetCouponSelection()
            }

            buttonConfirm.setOnClickListener {
                viewModel.selectedCoupon.value?.let { coupon ->
                    viewModel.onCouponSelected(coupon.value)
                }
                dialog.dismiss()
            }
        }
    }

    private fun observeDialogState(dialog: androidx.appcompat.app.AlertDialog) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                confirmationDialogView?.apply {
                    buttonConfirm.isEnabled = !state.isLoading
                    progressIndicator.isVisible = state.isLoading
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        confirmationDialog = null
        confirmationDialogView = null
        _binding = null
    }
}