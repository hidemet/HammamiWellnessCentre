package com.example.hammami.presentation.ui.features.payment

import android.content.Context
import android.os.Bundle
import android.text.Selection.setSelection
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hammami.R
import com.example.hammami.core.formatter.setupExpiryDateFormatting
import com.example.hammami.databinding.FragmentPaymentBinding
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.model.payment.PaymentMethod
import com.example.hammami.presentation.ui.features.BaseFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class PaymentFragment : BaseFragment() {
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!
    private val args: PaymentFragmentArgs by navArgs()
    private val viewModel: PaymentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paymentItem = args.paymentItem
        viewModel.setPaymentItem(paymentItem)
        setupUI()
        observeFlows()
    }

     override fun setupUI() {
        setupTopAppBar()
        setupPaymentDetails()
        setupDiscountSection()
        setupPaymentMethods()
        setupCreditCardFields()
        setupConfirmButton()
    }

    private fun setupTopAppBar() = with(binding) {
        topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupPaymentDetails() = with(binding) {
        viewModel.state.value.paymentItem?.let { item ->
            when (item) {
                is PaymentItem.ServiceBooking -> {
                    serviceBookingDetails.isVisible = true
                    giftCardDetails.isVisible = false
                    serviceName.text = item.title
                    serviceDateTime.text = item.dateTime.toString()
                    serviceDuration.text = getString(
                        R.string.service_duration_format,
                        item.duration
                    )
                }
                is PaymentItem.GiftCardPurchase -> {
                    serviceBookingDetails.isVisible = false
                    giftCardDetails.isVisible = true
                    giftCardValue.text = getString(
                        R.string.gift_card_value_format,
                        item.value
                    )
                }
            }
        }
    }

    private fun setupDiscountSection() = with(binding) {
        // Input per codice sconto
        discountInput.addTextChangedListener { text ->
            applyDiscountButton.isEnabled = !text.isNullOrBlank()
            viewModel.onDiscountCodeChanged(text.toString())
        }

        // Bottone per applicare lo sconto
        applyDiscountButton.setOnClickListener {
            hideKeyboard()
            viewModel.onApplyDiscount()
        }
        // Rimozione sconto
        removeDiscountButton.setOnClickListener {
            viewModel.onRemoveDiscount()
            // Reset input field
            discountInput.text = null
            discountInput.clearFocus()
        }
    }

    private fun setupPaymentMethods() = with(binding) {
        paymentMethodChips.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val method = when (checkedIds.first()) {
                R.id.creditCardChip -> PaymentMethod.CREDIT_CARD
                R.id.paypalChip -> PaymentMethod.PAYPAL
                R.id.googlePayChip -> PaymentMethod.GOOGLE_PAY
                else -> return@setOnCheckedStateChangeListener
            }
            viewModel.onPaymentMethodSelected(method)
        }
    }

    private fun setupCreditCardFields() = with(binding) {
        cardNumberInput.doAfterTextChanged { text ->
            viewModel.onCardDataChanged(number = text?.toString() ?: "")
        }

        // Gestione corretta della data di scadenza
        expiryInput.doAfterTextChanged { editable ->
            val input = editable?.toString() ?: ""
            if (input.length == 2 && !input.contains("/")) {
                editable?.apply {
                    insert(2, "/")
                }
            }
            viewModel.onCardDataChanged(expiry = editable?.toString() ?: "")
        }

        cvvInput.doAfterTextChanged { text ->
            viewModel.onCardDataChanged(cvv = text?.toString() ?: "")
        }
    }


    private fun setupConfirmButton() = with(binding) {
        confirmButton.setOnClickListener {
            viewModel.onConfirmPayment()
        }
    }

    private suspend fun observeState() {
        viewModel.state.collect { state ->
            updateLoadingState(state)
            updatePaymentItemDetails(state.paymentItem)
            updateDiscountSection(state)
            updatePaymentMethod(state)
            updateCardFields(state)
            updateSummary(state)
        }
    }


    private fun updateConfirmButtonState(state: PaymentUiState) {
        binding.confirmButton.isEnabled = when (state.selectedMethod) {
            PaymentMethod.CREDIT_CARD -> state.creditCard?.isValid() == true
            PaymentMethod.PAYPAL, PaymentMethod.GOOGLE_PAY -> true
        }
    }

    private fun updatePaymentItemDetails(paymentItem: PaymentItem) {
        with(binding) {
            when (paymentItem) {
                is PaymentItem.GiftCardPurchase -> {
                    // Nascondi dettagli servizio
                    serviceBookingDetails.isVisible = false
                    // Mostra dettagli gift card
                    giftCardDetails.isVisible = true
                    giftCardValue.text = getString(R.string.gift_card_value_format, paymentItem.value)
                }
                is PaymentItem.ServiceBooking -> {
                    // Nascondi dettagli gift card
                    giftCardDetails.isVisible = false
                    // Mostra dettagli servizio
                    serviceBookingDetails.isVisible = true
                    serviceName.text = paymentItem.title
                    serviceDateTime.text = paymentItem.dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    serviceDuration.text = getString(R.string.duration_format, paymentItem.duration)
                }
            }

            // Aggiorna importi
            originalAmount.text = getString(R.string.price_format, paymentItem.amount)
            totalAmount.text = getString(R.string.price_format, paymentItem.amount)
        }
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeEvents() }
                launch { observeState() }

            }
        }
    }

    private fun updateDiscountSection(state: PaymentUiState) = with(binding) {
        // Gestione del codice sconto
        discountInput.isEnabled = state.appliedDiscount == null
        applyDiscountButton.isEnabled = state.discountCode.isNotBlank() &&
                state.appliedDiscount == null

        // Visualizzazione dello sconto applicato
        appliedDiscountCard.isVisible = state.appliedDiscount != null
        state.appliedDiscount?.let { discount ->
            discountCode.text = discount.code
            discountAmount.text = getString(
                R.string.price_format_negative,
                discount.value
            )
        }

        // Reset in caso di errore
        if (state.discountError != null) {
            discountInput.text?.clear()
            discountInput.clearFocus()
        }
    }

    private fun updatePaymentMethod(state: PaymentUiState) = with(binding) {
        creditCardDetailsContainer.isVisible = state.selectedMethod == PaymentMethod.CREDIT_CARD
    }

    private fun updateCardFields(state: PaymentUiState) = with(binding) {
        // Mostra gli errori della carta se presenti
        state.cardErrors?.let { errors ->
            cardNumberLayout.error = errors.numberError?.asString(requireContext())
            expiryLayout.error = errors.expiryError?.asString(requireContext())
            cvvLayout.error = errors.cvvError?.asString(requireContext())
        } ?: run {
            // Reset degli errori
            cardNumberLayout.error = null
            expiryLayout.error = null
            cvvLayout.error = null
        }
    }

    private fun updateSummary(state: PaymentUiState) = with(binding) {
        state.paymentItem?.let { item ->
            // Importi
            originalAmount.text = getString(R.string.price_format, item.amount)

            val finalAmount = state.appliedDiscount?.let {
                item.amount - it.value
            } ?: item.amount
            totalAmount.text = getString(R.string.price_format, finalAmount)

            // Punti karma
            earnedPoints.text = getString(
                R.string.earned_points_format,
                state.earnedPoints
            )
            totalPoints.text = getString(
                R.string.total_points_format,
                state.totalPoints + state.earnedPoints
            )
        }
    }

    private fun updateLoadingState(state: PaymentUiState) = with(binding) {
        // Loading state
        confirmButton.isEnabled = !state.isLoading
        linearProgressIndicator.isVisible = state.isLoading
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    private fun handleEvent(event: PaymentEvent) {
        when (event) {
            is PaymentEvent.ShowError -> {
                showSnackbar(event.message.asString(requireContext()))
            }
            is PaymentEvent.NavigateToGiftCardGenerated -> {
                findNavController().navigate(
                    PaymentFragmentDirections
                        .actionPaymentFragmentToGiftCardGeneratedFragment(event.transactionId)
                )
            }
            is PaymentEvent.NavigateToBookingSummary -> {
                TODO()
          //      findNavController().navigate(
            //        PaymentFragmentDirections
              //          .actionPaymentFragmentToBookingSummaryFragment(event.transactionId)
                //)
            }
            PaymentEvent.NavigateBack -> findNavController().navigateUp()
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }


    private fun hideKeyboard() {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PAYMENT_ITEM = "payment_item"
    }
}