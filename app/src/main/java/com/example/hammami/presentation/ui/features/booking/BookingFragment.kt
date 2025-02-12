package com.example.hammami.presentation.ui.features.booking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.booking.BookingViewModel.*
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // Permesso concesso, procedi con la logica di prenotazione

                } else {
                    showSnackbar(UiText.StringResource(R.string.notification_permission_denied))
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
                launch { observeState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeState() {
        viewModel.uiState.collectLatest { state ->
            binding.progressBar.isVisible = state.isLoading
            binding.bookButton.isEnabled =
                state.selectedDate != null && state.selectedTimeSlot != null && !state.isLoading

            // Gestione visibilità del messaggio per i giorni di chiusura
            binding.closedDayMessage.isVisible = state.isClosedDay
            binding.timeSlotsChipGroup.isVisible = !state.isClosedDay

            state.selectedDate?.let {
                val calendar = Calendar.getInstance().apply {
                    set(it.year, it.monthValue - 1, it.dayOfMonth)
                }
                binding.calendarView.date = calendar.timeInMillis
            }

            binding.timeSlotsChipGroup.apply {
                removeAllViews()
                state.availableTimeSlots.forEach { timeSlot ->
                    val chip = createTimeSlotChip(timeSlot)
                    if (timeSlot == state.selectedTimeSlot) {
                        chip.isChecked = true
                    }
                    addView(chip)
                }
                // Se c'è uno slot selezionato, assicurati che sia visibile
                state.selectedTimeSlot?.let { selectedTimeSlot ->
                    for (i in 0 until childCount) {
                        val chip = getChildAt(i) as Chip
                        if (chip.text == DateTimeUtils.formatTime(selectedTimeSlot.startTime)) {
                            chip.isChecked = true
                            break
                        }
                    }
                }
            }
        }
    }

    private suspend fun observeEvents() {
        viewModel.uiEvent.collect{ event ->
            when (event) {
                is BookingUiEvent.ShowError -> showSnackbar(event.message)
                is BookingUiEvent.ShowUserMassage -> showSnackbar(event.message)
                is BookingUiEvent.NavigateToPayment -> {
                    Log.d(
                        "BookingFragment",
                        "Navigating to PaymentFragment with bookingId: ${event.paymentItem.bookingId}"
                    )
                    navigateToPaymentFragment(event.paymentItem)
                }
            }
        }
    }

    private fun navigateToPaymentFragment(paymentItem: PaymentItem) {
        val currentDestination = findNavController().currentDestination?.id
        if (currentDestination == R.id.bookingFragment) {
            val action = BookingFragmentDirections.actionBookingFragmentToPaymentFragment(paymentItem)
            findNavController().navigate(action)
        } else {
            Log.e("Navigation", "Current destination is not BookingFragment")
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
            if (hasNotificationPermission()) {
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
        val isClosed = (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.MONDAY)

        if (isClosed) {
            // Mostra il messaggio che il giorno è chiuso
            binding.timeSlotsChipGroup.removeAllViews()  // Svuota eventuali chip precedenti
            binding.closedDayMessage.visibility = View.VISIBLE // Mostra il messaggio
            binding.timeSlotsChipGroup.visibility = View.GONE // Nascondi il ChipGroup
        } else {
            // Nascondi il messaggio e procedi con la selezione della data
            binding.closedDayMessage.visibility = View.GONE
            binding.timeSlotsChipGroup.visibility = View.VISIBLE //Assicurati che sia visibile

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