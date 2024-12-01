package com.example.hammami.presentation.ui.features.userProfile.coupon


import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.databinding.FragmentCouponBinding
import com.example.hammami.presentation.ui.adapters.ActiveVoucherAdapter
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.userProfile.coupon.CouponViewModel.*

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// CouponFragment.kt
@AndroidEntryPoint
class CouponFragment : BaseFragment() {
    private var _binding: FragmentCouponBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CouponViewModel by activityViewModels()
    private val activeCouponsAdapter by lazy { createActiveCouponsAdapter() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCouponBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        setupTopAppBar()
        setupRecyclerView()
        setupFab()
        setupInitialState()

    }

    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    private fun createActiveCouponsAdapter() = ActiveVoucherAdapter(
        onCopyCode = { code -> viewModel.copyCouponToClipboard(code) }
    )

    private fun setupRecyclerView() = with(binding) {
        rvActiveCoupons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activeCouponsAdapter
            isNestedScrollingEnabled = false
            addItemDecoration(createItemDecoration())
        }
    }

    private fun setupFab() = with(binding) {
        fabRedeemCoupon.setOnClickListener {
            findNavController().navigate(R.id.action_couponFragment_to_couponSelectionFragment)
        }
    }



    // Funzione chiamata quando un coupon è stato generato
    fun onCouponGenerated() {
        viewModel.loadData() // Ricarica i dati
}


    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeEvents() }
                launch { observeState() }

            }
        }
    }

    private suspend fun observeState() {
        viewModel.uiState.collect { state ->
            updateUI(state)
        }
    }

    private suspend fun observeEvents() {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowError -> showSnackbar(event.message)
                is UiEvent.ShowUserMessage -> showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    private fun updateUI(state: UiState) = with(binding) {
        progressIndicator.isVisible = state.isLoading
        updatePointsDisplay(state.userPoints)
        updateCouponsList(state)
    }

    private fun updatePointsDisplay(points: Int) = with(binding) {
        tvPoints.text = getString(R.string.points, points)
    }

    private fun updateCouponsList(state: UiState) = with(binding) {
        activeCouponsTitle.isVisible = state.hasActiveCoupons
        rvActiveCoupons.isVisible = state.hasActiveCoupons
        noActiveCoupons.isVisible = !state.hasActiveCoupons && !state.isLoading
        activeCouponsAdapter.submitList(state.activeCoupons)
    }

    private fun setupInitialState() = with(binding) {
        progressIndicator.isVisible = true
        activeCouponsTitle.isVisible = false
        rvActiveCoupons.isVisible = false
        noActiveCoupons.isVisible = false
    }





    private fun createItemDecoration() = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = resources.getDimensionPixelSize(R.dimen.spacing_small)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("CouponFragment", "onResume")
        viewModel.loadData()
    }

    override fun onPause() {
        super.onPause()
        Log.d("CouponFragment", "onPause")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}