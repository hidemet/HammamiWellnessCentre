package com.example.hammami.presentation.ui.features.payment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hammami.R
import com.example.hammami.core.formatter.CardInputFormatter
import com.example.hammami.core.formatter.setupCardNumberFormatting
import com.example.hammami.core.formatter.setupExpiryDateFormatting
import com.example.hammami.databinding.FragmentPaymentBinding

import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.domain.model.payment.PaymentMethod
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PaymentFragment : BaseFragment() {
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!
    private val args: PaymentFragmentArgs by navArgs()

    @Inject
    lateinit var paymentViewModelFactory: PaymentViewModelFactory

    private val viewModel: PaymentViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return paymentViewModelFactory.create(args.paymentItem) as T
            }
        }
    }

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
        //viewModel.setPaymentItem(args.paymentItem)
        setupUI()
        observeFlows()
    }

    override fun setupUI() = with(binding) {
        setupTopAppBar()
        setupPaymentItem()
        setupDiscountSection()
        setupSummarySection()
        setupPaymentMethods()
        setupCreditCardFields()
        setupConfirmButton()

    }

    private fun setupTopAppBar() = with(binding) {
        topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupSummarySection() = with(binding) {
        // Importi
        originalAmount.text = getString(
            R.string.price_format,
            viewModel.state.value.paymentItem.price
        )
        totalAmount.text = getString(
            R.string.price_format,
            viewModel.state.value.paymentItem.price
        )

        // Punti karma
        earnedPoints.text = getString(
            R.string.earned_points_format,
            viewModel.state.value.earnedPoints
        )
        totalPoints.text = getString(
            R.string.total_points_format,
            viewModel.state.value.totalPoints + viewModel.state.value.earnedPoints
        )
    }


    private fun setupPaymentItem() = with(binding) {
        when (val item = viewModel.state.value.paymentItem) {
            is PaymentItem.ServiceBookingPayment -> {
                serviceBookingDetails.isVisible = true
                giftCardDetails.isVisible = false
                serviceName.text = item.title
                serviceDateTime.text = item.dateTime.toString()
                serviceDuration.text = getString(R.string.service_duration_format, item.duration)
            }

            is PaymentItem.GiftCardPayment -> {
                serviceBookingDetails.isVisible = false
                giftCardDetails.isVisible = true
                giftCardValue.text = getString(R.string.gift_card_value_format, item.price)
            }
        }
    }

    private fun setupDiscountSection() = with(binding) {
        discountInput.addTextChangedListener { text ->
            applyDiscountButton.isEnabled = !text.isNullOrBlank()
            viewModel.onDiscountCodeChanged(text.toString())
        }

        applyDiscountButton.setOnClickListener {
            hideKeyboard()
            viewModel.onApplyVoucher()
        }

        removeDiscountButton.setOnClickListener {
            viewModel.onRemoveVoucher()
            discountInput.text = null
            discountInput.clearFocus()
        }
    }

    private fun setupPaymentMethods() = with(binding) {

        // Seleziona la carta di credito di default
        binding.creditCardChip.isChecked = true
        viewModel.onPaymentMethodSelect(PaymentMethod.CREDIT_CARD)

        paymentMethodChips.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val method = when (checkedIds.first()) {
                R.id.creditCardChip -> PaymentMethod.CREDIT_CARD
                R.id.paypalChip -> PaymentMethod.PAYPAL
                R.id.googlePayChip -> PaymentMethod.GOOGLE_PAY
                else -> return@setOnCheckedStateChangeListener
            }
            viewModel.onPaymentMethodSelect(method)
        }
    }

    private fun setupCreditCardFields() = with(binding) {
        cardNumberInput.setupCardNumberFormatting(
            formatter = CardInputFormatter()
        ) { text ->
            viewModel.onCardDataChanged(number = text)
        }

        expiryInput.setupExpiryDateFormatting(
            formatter = CardInputFormatter()
        ) { text ->
            viewModel.onCardDataChanged(expiry = text)
        }

        cvvInput.doAfterTextChanged { text ->
            viewModel.onCardDataChanged(cvv = text?.toString() ?: "")
        }
    }

    private fun setupConfirmButton() = with(binding) {
        confirmButton.setOnClickListener {
            Log.d("PaymentFragment", "Confirm button clicked")
            viewModel.onConfirmPayment()
        }
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Log per debug
                Log.d("PaymentFragment", "Starting to observe flows")
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

    private fun updateUI(state: PaymentUiState) {
        updateLoadingState(state.isLoading)
        updatePaymentSummary(state)
        updateDiscountSection(state)
        updatePaymentMethod(state)
        updateCreditCardErrors(state)
        updateConfirmButton(state.isPaymentEnabled)
    }

    private fun updatePaymentSummary(state: PaymentUiState) = with(binding) {
        originalAmount.text = getString(R.string.price_format, state.paymentItem.price)
        totalAmount.text = getString(R.string.price_format, state.finalAmount)
        earnedPoints.text = getString(R.string.earned_points_format, state.earnedPoints)
        totalPoints.text = getString(
            R.string.total_points_format,
            state.totalPoints + state.earnedPoints
        )
    }

    private fun updateDiscountSection(state: PaymentUiState) = with(binding) {
       // discountInput.isEnabled = state.appliedVoucher == null
        applyDiscountButton.isEnabled = state.discountCode.isNotBlank() &&
                state.appliedVoucher == null
        textFieldDiscountVoucher.error = state.discountError?.asString(requireContext())

        appliedDiscountCard.isVisible = state.appliedVoucher != null
        state.appliedVoucher?.let { voucher ->
            discountCode.text = voucher.code
            discountAmount.text = getString(R.string.price_format_negative, voucher.value)
        }

        state.discountError?.let {
            discountInput.text?.clear()
            discountInput.clearFocus()
        }
    }

    private fun updateConfirmButton(enabled: Boolean) {
        binding.confirmButton.isEnabled = enabled
    }

    private fun updatePaymentMethod(state: PaymentUiState) = with(binding) {
        creditCardDetailsContainer.isVisible = state.selectedMethod == PaymentMethod.CREDIT_CARD
    }

    private fun updateCreditCardErrors(state: PaymentUiState) = with(binding) {
        state.cardValidationErrors?.let {
            cardNumberLayout.error = it.numberError?.asString(requireContext())
            expiryLayout.error = it.expiryError?.asString(requireContext())
            cvvLayout.error = it.cvvError?.asString(requireContext())
        }
    }


    private fun updateLoadingState(isLoading: Boolean) = with(binding) {
        confirmButton.isEnabled = !isLoading
        linearProgressIndicator.isVisible = isLoading
    }

    private suspend fun observeEvents() {
        var isNavigating = false
        viewModel.event.collect { event ->
            if (!isNavigating) {
                when (event) {
                    is PaymentEvent.NavigateToGiftCardGenerated -> {
                        isNavigating = true
                        navigateToGiftCardGenerated(event.transactionId)
                    }
                    is PaymentEvent.ShowError -> showSnackbar(event.message)
                    else -> Unit
                }
            }
        }
    }

    private fun navigateToGiftCardGenerated(transactionId: String) {
        val currentDestination = findNavController().currentDestination?.id
        if (currentDestination == R.id.paymentFragment) {
            findNavController().navigate(
                PaymentFragmentDirections.actionPaymentFragmentToGiftCardGeneratedFragment(transactionId)
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}