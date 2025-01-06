package com.example.hammami.presentation.ui.features.payment

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
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
import com.example.hammami.domain.usecase.booking.GetBookingByIdUseCase
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.NotificationReceiverFragment.Companion.REQUEST_CODE_POST_NOTIFICATIONS
//import com.example.hammami.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import com.example.hammami.core.result.Result
import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.hammami.presentation.ui.features.NotificationReceiverFragment
import java.util.Calendar
import java.util.TimeZone


@AndroidEntryPoint
class PaymentFragment : BaseFragment() {
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!
    private val args: PaymentFragmentArgs by navArgs()

    @Inject
    lateinit var getBookingByIdUseCase: GetBookingByIdUseCase

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
        setupUI()
        observeFlows()
    }

    override fun setupUI() {
        setupTopAppBar()
        setupPaymentItem()
        setupDiscountSection()
        setupBottomSheet()
        setupPaymentMethods()
        setupCreditCardFields()
    }

    private fun setupTopAppBar() = with(binding) {
        topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupPaymentItem() = with(binding) {
        when (val item = viewModel.state.value.paymentItem) {
            is PaymentItem.ServiceBookingPayment -> {
                serviceBookingDetails.isVisible = true
                giftCardDetails.isVisible = false
                bookingCard.serviceName.text = item.serviceName
                bookingCard.bookingDate.text = item.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                bookingCard.bookingTime.text = "${item.startTime} - ${item.endTime}"
                // TODO("Convertire la durata startTime-endTime in minuti oppure cambiare il formato del layout")
            }

            is PaymentItem.GiftCardPayment -> {
                serviceBookingDetails.isVisible = false
                giftCardDetails.isVisible = true
                giftCardValue.text = getString(R.string.gift_card_value_format, item.price)
            }
        }
    }

    private fun setupDiscountSection() = with(binding) {
        discountCodeEditText.addTextChangedListener { text ->
            applyDiscountButton.isEnabled = !text.isNullOrBlank()
            viewModel.onDiscountCodeChanged(text.toString())
            discountCodeInput.error = null // Rimuove l'errore quando l'utente digita il codice
        }

        applyDiscountButton.setOnClickListener {
            //hideKeyboard()    <-------------------    COMMENTATO PERCHE' NON RICONOSCE hideKeyboard()
            viewModel.onApplyVoucher()
        }

        appliedDiscountChip.setOnClickListener {
            viewModel.onRemoveVoucher()
            clearDiscountInput()
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
            checkAllFieldsFilled()
        }
    }

    private fun setupCreditCardFields() = with(binding) {
        cardNumberInput.setupCardNumberFormatting(formatter = CardInputFormatter()) { number ->
            viewModel.onCardDataChanged(number = number)
            checkAllFieldsFilled()
        }

        // Formattazione della data di scadenza
        expiryInput.setupExpiryDateFormatting(formatter = CardInputFormatter()) { expiry ->
            viewModel.onCardDataChanged(expiry = expiry)
            checkAllFieldsFilled()
        }

        cvvInput.doAfterTextChanged { text ->
            viewModel.onCardDataChanged(cvv = text?.toString())
            checkAllFieldsFilled()
        }
    }

    private fun checkAllFieldsFilled() = with(binding) {
        val isCreditCardSelected = viewModel.state.value.selectedMethod == PaymentMethod.CREDIT_CARD
        checkoutConfirmButton.isEnabled = if (isCreditCardSelected) {
            cardNumberInput.text.toString().isNotBlank() &&
                    expiryInput.text.toString().isNotBlank() &&
                    cvvInput.text.toString().isNotBlank()
        } else {
            true // Abilita il bottone se il metodo di pagamento non è la carta di credito
        }
    }

    private fun clearDiscountInput() = with(binding) {
        discountCodeEditText.apply {
            setText("")
            isEnabled = true
            clearFocus()
        }
        binding.discountCodeInput.error = null
    }


    private fun setupBottomSheet() = with(binding) {
        checkoutConfirmButton.setOnClickListener {
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
        updateBottomSheet(state)
        updateDiscountSection(state)
        updatePaymentMethod(state)
        updateCreditCardErrors(state)
    }

    private fun updateBottomSheet(state: PaymentUiState) = with(binding) {
        // Gestione prezzi
        if (state.showPriceBreakdown) {
            itemPriceLabel.isVisible = true
            itemPrice.isVisible = true
            discountLabel.isVisible = true
            discountAmount.isVisible = true
            itemPrice.text = getString(R.string.price_format, state.itemPrice)
            discountAmount.text = getString(R.string.price_format_negative, state.discountValue)
        } else {
            itemPriceLabel.isVisible = false
            itemPrice.isVisible = false
            discountLabel.isVisible = false
            discountAmount.isVisible = false
        }

        // Importo totale e punti karma sempre visibili
        finalAmount.text = getString(R.string.price_format, state.finalAmount)
        earnedKarmaPoints.text = getString(R.string.earned_points_format, state.earnedKarmaPoints)
        newKarmaPointsBalance.text =
            getString(R.string.total_points_format, state.newKarmaPointsBalance)

        //Bottone per confermare il pagamento
        checkoutConfirmButton.isEnabled = state.isPaymentEnabled
    }

    private fun updateDiscountSection(state: PaymentUiState) = with(binding) {
        // Aggiorniamo sempre l'errore dal TextInputLayout
        discountCodeInput.error = state.discountError?.asString(requireContext())
        // Gestiamo lo stato del pulsante
        applyDiscountButton.isEnabled = state.discountCode.isNotBlank() &&
                state.appliedVoucher == null
        // Gestiamo la card del voucher applicato
        appliedDiscountContainer.isVisible = state.appliedVoucher != null
        discountCodeInput.isVisible = state.appliedVoucher == null
        applyDiscountButton.isVisible = state.appliedVoucher == null

        state.appliedVoucher?.let { voucher ->
            appliedDiscountChip.text = voucher.code
        }
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


    private fun updateLoadingState(isLoading: Boolean) {
        binding.linearProgressIndicator.isVisible = isLoading
    }

    private suspend fun observeEvents() {
        viewModel.event.collect { event ->
            when (event) {
                is PaymentEvent.NavigateToGiftCardGenerated -> {
                    navigateToGiftCardGenerated(event.giftCardId)
                }

                is PaymentEvent.NavigateToBookingSummary -> {
                    notifiche(event.bookingId)
                    navigateToBookingSummary(event.bookingId)
                }

                is PaymentEvent.ShowError -> showSnackbar(event.message)
                PaymentEvent.NavigateBack -> TODO()
            }
        }
    }

    private suspend fun notifiche(bookingId: String) {
        when (val result = getBookingByIdUseCase(bookingId)) {
            is Result.Error -> {
                // Gestisci l'errore
            }

            is Result.Success -> {
                val bookingDateMillis = result.data.dateMillis
                val notificationTime = bookingDateMillis - 24 * 60 * 60 * 1000 // 24 hours before the booking time

                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        REQUEST_CODE_POST_NOTIFICATIONS
                    )
                    return
                }
                scheduleNotification(requireContext(), notificationTime, bookingId)
            }

        }
    }

    fun getTimestampFromDateAndTime(day: Int, month: Int, year: Int, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))  //    <-------------------------------    CONTROLLA TIMEZONE
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    fun scheduleNotification(context: Context, notificationTimeIng: Long, appointmentId: String) {
        //val appointmentTimestamp = getTimestampFromDateAndTime(day, month, year, hour, minute)
        val notificationTime = System.currentTimeMillis() + 5000    //per testare il funzionamento senza dover aspettare 24 ore prima usare questo: val notificationTime = System.currentTimeMillis() + 5000

        Log.d("NotificationTest", "Scheduling notification for appointmentId: $appointmentId at time: $notificationTime ( ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(java.util.Date(notificationTime))} )")

        val intent = Intent(context, NotificationReceiverFragment::class.java).apply {
            putExtra("appointmentId", appointmentId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
                return
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notificationTime,
            pendingIntent
        )
    }

    private fun navigateToBookingSummary(bookingId: String) {
        val currentDestination = findNavController().currentDestination?.id
        if (currentDestination == R.id.paymentFragment) {
            findNavController().navigate(
                    PaymentFragmentDirections.actionPaymentFragmentToBookingSummaryFragment(bookingId)
            )
        }
    }

    private fun navigateToGiftCardGenerated(giftCardId: String) {
        val currentDestination = findNavController().currentDestination?.id
        if (currentDestination == R.id.paymentFragment) {
            findNavController().navigate(
                PaymentFragmentDirections.actionPaymentFragmentToGiftCardSummaryFragment(
                    giftCardId
                )
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

