package com.example.hammami.presentation.ui.features.userProfile.giftCard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hammami.R
import com.example.hammami.databinding.FragmentGiftCardGeneratedBinding
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.userProfile.giftCard.GiftCardViewModel.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        setupGiftCardDetails()
        setupButtons()
    }

    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            // Torna alla lista delle gift card
            findNavController().popBackStack(R.id.giftCardsFragment, false)
        }
    }

    private fun setupGiftCardDetails() = with(binding.giftCard) {

        viewModel.state.value.generatedGiftCard?.let { giftCard ->
            // Imposta il valore della gift card
            giftCardValue.text = getString(
                R.string.gift_card_value_format,
                giftCard.value
            )
            // Imposta il codice della gift card
            giftCardCode.setText(giftCard.code)
            // Imposta la data di scadenza
            giftCardExpiry.text = getString(
                R.string.expires_on,
                giftCard.expirationDate
            )
            // Setup del pulsante di copia
            giftCardCodeLayout.setEndIconOnClickListener {
                viewModel.copyGiftCardToClipboard(giftCard.code)
            }
        }
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
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is UiEvent.ShowMessage -> {
                        showSnackbar(event.message)
                    }
                    else -> Unit
                }
            }
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