package com.example.hammami.presentation.ui.features.userProfile.giftCard

import android.graphics.Rect
import android.os.Bundle
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
import com.example.hammami.databinding.FragmentGiftCardsBinding
import com.example.hammami.presentation.ui.adapters.ActiveGiftCardAdapter
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GiftCardsFragment : BaseFragment() {
    private var _binding: FragmentGiftCardsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GiftCardViewModel by activityViewModels()
    private val activeGiftCardsAdapter by lazy { createActiveGiftCardsAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGiftCardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        setupAppBar()
        setupRecyclerView()
        setupFab()
        setupInitialState()
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
        viewModel.state.collect { state ->
            updateUI(state)
        }
    }

    private suspend fun observeEvents() {
        viewModel.uiEvent.collect { event ->
            handleEvent(event)
        }
    }

    private fun updateUI(state: GiftCardViewModel.GiftCardState) = with(binding) {
        progressIndicator.isVisible = state.isLoading
        updateGiftCardsList(state)
    }

    private fun updateGiftCardsList(state: GiftCardViewModel.GiftCardState) = with(binding) {
        noGiftCards.isVisible = !state.hasGiftCards && !state.isLoading
        activeGiftCardsTitle.isVisible = state.hasGiftCards
        giftCardsRecyclerView.isVisible = state.hasGiftCards

        activeGiftCardsAdapter.submitList(state.userGiftCards)
    }

    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    private fun createActiveGiftCardsAdapter() = ActiveGiftCardAdapter { code ->
        viewModel.copyGiftCardToClipboard(code)
    }

    private fun setupRecyclerView() = with(binding) {
        giftCardsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activeGiftCardsAdapter
            isNestedScrollingEnabled = false
            addItemDecoration(createItemDecoration())
        }
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

    private fun setupFab() = with(binding) {
        fabPurchaseGiftCard.setOnClickListener {
            findNavController().navigate(R.id.action_giftCardsFragment_to_availableGiftCardsFragment)
        }
    }

    private fun setupInitialState() = with(binding) {
        progressIndicator.isVisible = true
        activeGiftCardsTitle.isVisible = false
        giftCardsRecyclerView.isVisible = false
        noGiftCards.isVisible = false
    }

    private fun handleEvent(event: GiftCardViewModel.UiEvent) {
        when (event) {
            is GiftCardViewModel.UiEvent.ShowMessage -> showSnackbar(event.message)
            is GiftCardViewModel.UiEvent.ShowError -> showSnackbar(event.message)
           // is GiftCardViewModel.UiEvent.GiftCardPurchaseSuccess -> {
            //    findNavController().navigate(R.id.action_availableGiftCardsFragment_to_giftCardGeneratedFragment)
            // }

            is GiftCardViewModel.UiEvent.NavigateToPayment -> TODO()
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}