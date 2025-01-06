package com.example.hammami.presentation.ui.features.userProfile.giftCard

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
    private val activeGiftCardsAdapter by lazy {
        ActiveGiftCardAdapter(
            onCopyCode = { code -> viewModel.copyGiftCardToClipboard(code) }
        )
    }

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
        viewModel.uiState.collect { state ->
            Log.d("GiftCardsFragment", "State updated: ${state.userGiftCards.size} gift cards")
            binding.progressIndicator.isVisible = state.isLoading
            updateGiftCardsList(state)
        }
    }

    private suspend fun observeEvents() {
        viewModel.uiEvent.collect { event ->
            handleEvent(event)
        }
    }

    private fun updateGiftCardsList(state: GiftCardViewModel.GiftCardState) = with(binding) {
        Log.d("GiftCardsFragment", "Updating UI with ${state.userGiftCards.size} gift cards")

        // Aggiorna la lista nell'adapter
        activeGiftCardsAdapter.submitList(state.userGiftCards)

        // Aggiorna la visibilità delle view
        noGiftCards.isVisible = state.userGiftCards.isEmpty()
        activeGiftCardsContainer.isVisible = state.userGiftCards.isNotEmpty()
        activeGiftCardsTitle.isVisible = state.userGiftCards.isNotEmpty()
        giftCardsRecyclerView.isVisible = state.userGiftCards.isNotEmpty()

        // Debug
        Log.d("GiftCardsFragment", "Container visibility: ${activeGiftCardsContainer.isVisible}")
        Log.d("GiftCardsFragment", "RecyclerView visibility: ${giftCardsRecyclerView.isVisible}")
        Log.d("GiftCardsFragment", "No gift cards visibility: ${noGiftCards.isVisible}")
    }

    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    private fun setupRecyclerView() = with(binding) {
        giftCardsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activeGiftCardsAdapter
            isNestedScrollingEnabled = false
            addItemDecoration(createItemDecoration())
        }
        Log.d("GiftCardsFragment", "RecyclerView setup completed with adapter: $activeGiftCardsAdapter")

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
           // is GiftCardViewModel.UiEvent.NavigateToPayment -> TODO()
        }
    }
    override fun onResume() {
        super.onResume()
        Log.d("GiftCardsFragment", "onResume: reloading data")
        viewModel.loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}