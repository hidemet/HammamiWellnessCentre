package com.example.hammami.presentation.ui.features.userProfile.giftCard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hammami.R
import com.example.hammami.databinding.FragmentGiftCardGeneratedBinding
import com.example.hammami.domain.model.Voucher
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.userProfile.giftCard.GiftCardViewModel.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class GiftCardGeneratedFragment : BaseFragment() {
    private var _binding: FragmentGiftCardGeneratedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GiftCardViewModel by activityViewModels()
    private val args: GiftCardGeneratedFragmentArgs by navArgs()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recupera l'ID della transazione dagli argomenti
        viewModel.loadGiftCard(args.transactionId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGiftCardGeneratedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        setupTopAppBar()
        setupButtons()
    }

    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            // Torna alla lista delle gift card
            findNavController().popBackStack(R.id.giftCardsFragment, false)
        }
    }

    private fun updateGiftCardUI(giftCard: Voucher) = with(binding.giftCard) {
        titleText.text = getString(R.string.gift_card)

        voucherValue.text = getString(
            R.string.gift_card_value_format,
            giftCard.value
        )

        voucherCode.setText(giftCard.code)

        voucherExpiry.text = getString(
            R.string.expires_on,
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(giftCard.expirationDate.toDate())
        )

        voucherCodeLayout.setEndIconOnClickListener {
            viewModel.copyGiftCardToClipboard(giftCard.code)
        }

        icon.setImageResource(R.drawable.ic_gift_card)
    }

    private fun setupButtons() = with(binding) {
        buttonGoToHome.setOnClickListener {
            findNavController().navigate(R.id.action_global_home)
        }
        buttonBackToGiftCards.setOnClickListener {
            findNavController().popBackStack(R.id.giftCardsFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeEvents() {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    showSnackbar(event.message)
                }
                else -> Unit
            }
        }
    }

    private suspend fun observeState() {
        viewModel.generatedGiftCard.collect { giftCard ->
            giftCard?.let { updateGiftCardUI(it) }
        }
    }

    companion object {
        private const val ARG_TRANSACTION_ID = "transaction_id"

        fun newInstance(transactionId: String) = GiftCardGeneratedFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TRANSACTION_ID, transactionId)
            }
        }
    }
}