package com.example.hammami.presentation.ui.features.booking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hammami.R
import com.example.hammami.core.time.DateTimeUtils
import com.example.hammami.core.time.TimeSlot
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.FragmentBookingBinding
import com.example.hammami.domain.model.Service
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.booking.BookingViewModel.*
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class BookingFragment : BaseFragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private val args: BookingFragmentArgs by navArgs()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    @Inject
    lateinit var bookingViewModelFactory: BookingViewModelFactory

    private val viewModel: BookingViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return bookingViewModelFactory.create(args.service) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inizializza il launcher per la richiesta di permessi
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // Permesso concesso, procedi con la logica di prenotazione
                    viewLifecycleOwner.lifecycleScope.launch { viewModel.onConfirmBooking() }

                } else {
                    // Permesso negato, mostra un messaggio all'utente
                    showSnackbar(UiText.StringResource(R.string.notification_permission_denied))
                    // Potresti disabilitare la funzionalità di prenotazione, ecc.
                }
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setService(args.service) // Aggiungi questa chiamata
        setupUI()
        observeFlows()
        //viewModel.loadBooking(args.bookingId)  <-- Rimuovi questa riga se presente!
    }

    private fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNotificationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun setupUI() {
        setupTopAppBar()
        setupListeners()
        binding.serviceNameTextView.text = args.service.name

        // Imposta la data corrente sul CalendarView
        binding.calendarView.date = Calendar.getInstance().timeInMillis
        updateTimeSlots(Calendar.getInstance())
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { updateUiState(it) } }
                launch { viewModel.uiEvent.collect { observeUiEvents(it) } }
            }
        }
    }

    private fun observeUiEvents(event: BookingUiEvent) {

        when (event) {
            is BookingUiEvent.ShowError -> showSnackbar(event.message)
            is BookingUiEvent.ShowUserMassage -> showSnackbar(event.message)
            is BookingUiEvent.NavigateToPayment -> {
                navigateToPayment(event.paymentItem)
                viewModel.resetState()
            }
        }
    }

    private fun updateUiState(uiState: BookingUiState) {
        binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
        binding.bookButton.isEnabled =
            uiState.selectedDate != null && uiState.selectedTimeSlot != null && !uiState.isLoading

        binding.timeSlotsChipGroup.apply {
            removeAllViews() // Pulisce le chip precedenti
            uiState.availableTimeSlots.forEach { timeSlot ->
                // Aggiunge una chip per ogni slot
                addView(createTimeSlotChip(timeSlot))
            }

            // Imposta come selezionata la chip corrispondente all'orario di inizio selezionato
            uiState.selectedTimeSlot?.let { selectedTimeSlot ->
                for (i in 0 until childCount) {
                    val chip = getChildAt(i) as Chip
                    if (chip.text == DateTimeUtils.formatTime(selectedTimeSlot.startTime)) {
                        chip.isChecked = true
                        break
                    }
                }
            } ?: run {
                // Deseleziona tutte le chip se non c'è un orario selezionato
                binding.timeSlotsChipGroup.clearCheck()
            }

        }
    }

    private fun createTimeSlotChip(timeSlot: TimeSlot): Chip {
        return Chip(requireContext()).apply {
            text = DateTimeUtils.formatTime(timeSlot.startTime)
            isCheckable = true
            setOnClickListener { viewModel.onTimeSlotSelected(timeSlot) }
            Log.d("BookingFragment", "Creating chip with text: ${this.text}")
        }
    }

    private fun navigateToPayment(paymentItem: PaymentItem.ServiceBookingPayment) {
        Log.d(
            "BookingFragment",
            "Navigating to PaymentFragment with bookingId: ${paymentItem.bookingId}"
        )
        findNavController().navigate(
            BookingFragmentDirections.actionBookingFragmentToPaymentFragment(paymentItem)
        )
    }

    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }


    private fun setupListeners() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            handleDateChange(calendar)
        }

        binding.bookButton.setOnClickListener {
            if (hasNotificationPermission()) { //usa il metodo creato prima per controllare se ha già i permessi
                lifecycleScope.launch {
                    viewModel.onConfirmBooking()
                }
            } else {
                requestNotificationPermission()
            }
        }
    }

    private fun handleDateChange(calendar: Calendar) {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        Log.d("BookingFragment", "handleDateChange: dayOfWeek = $dayOfWeek") // AGGIUNGI QUESTO
        if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.MONDAY) {
            // Pulisci gli slot e mostra il messaggio
            binding.timeSlotsChipGroup.removeAllViews()
            showSnackbar(UiText.StringResource(R.string.giorni_chiusura))
        } else {
            val selectedDate = LocalDate.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            Log.d("BookingFragment", "Selected date: $selectedDate") // LOG

            viewModel.onDateSelected(selectedDate)
        }
    }

    private fun updateTimeSlots(calendar: Calendar) {
        val selectedDate = LocalDate.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        viewModel.onDateSelected(selectedDate)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}