package com.example.hammami.presentation.ui.features.userProfile.giftCard

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
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.R
import com.example.hammami.databinding.FragmentAvailableGiftCardsBinding
import com.example.hammami.domain.model.giftCard.AvailableGiftCard
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.presentation.ui.adapters.AvailableGiftCardAdapter
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AvailableGiftCardsFragment : BaseFragment() {
    private var _binding: FragmentAvailableGiftCardsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GiftCardViewModel by activityViewModels()
    private lateinit var giftCardAdapter: AvailableGiftCardAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAvailableGiftCardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        setupTopAppBar()
        setupRecyclerView()
        // viewModel.loadData()
    }

    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        giftCardAdapter = AvailableGiftCardAdapter { navController, giftCard ->
            navigateToPayment(navController, giftCard)
        }

        binding.rvAvailableGiftCards.apply {
            adapter = giftCardAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun navigateToPayment(navController: NavController, giftCard: AvailableGiftCard) {
        val paymentItem = giftCard.toPaymentItem()
        val direction = AvailableGiftCardsFragmentDirections
            .actionAvailableGiftCardsFragmentToPaymentFragment(paymentItem)
        navController.navigate(direction)
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeState() {
        viewModel.state.collect { state ->
            Log.d(
                "AvailableGiftCards",
                "State: isLoading=${state.isLoading}, cards=${state.availableGiftCards}"
            )
            binding.linearProgressIndicator.isVisible = state.isLoading
            giftCardAdapter.submitList(state.availableGiftCards)
        }
    }

    private suspend fun observeEvents() {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is GiftCardViewModel.UiEvent.ShowError -> showSnackbar(event.message)
                is GiftCardViewModel.UiEvent.ShowMessage -> showSnackbar(event.message)
                is GiftCardViewModel.UiEvent.GiftCardPurchaseSuccess -> {
                    findNavController().navigate(R.id.action_availableGiftCardsFragment_to_giftCardGeneratedFragment)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}