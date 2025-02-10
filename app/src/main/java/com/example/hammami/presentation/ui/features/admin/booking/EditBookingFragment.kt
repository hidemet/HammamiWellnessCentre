package com.example.hammami.presentation.ui.features.admin.booking

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.navigateUp
import com.example.hammami.R
import com.example.hammami.core.time.DateTimeUtils
import com.example.hammami.core.time.TimeSlot
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.FragmentBookingBinding
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.admin.booking.EditBookingViewModel.EditBookingUiState
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

@AndroidEntryPoint
class EditBookingFragment : BaseFragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditBookingViewModel by viewModels()
    private val args: EditBookingFragmentArgs by navArgs()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeFlows()
        viewModel.loadBooking(args.bookingId)

        findNavController().addOnDestinationChangedListener { controller, destination, arguments ->
            Log.d("EditBookingFragment", "Navigated to: ${destination.label}")
            // Puoi anche stampare gli argomenti se necessario:
            // Log.d("EditBookingFragment", "Arguments: $arguments")
        }
    }

    override fun setupUI() {
        setupTopAppBar()
        binding.topAppBar.title = getString(R.string.edit_booking_title)
        binding.bookButton.text = getString(R.string.save_changes)

        setupListeners()
    }

    private fun setupTopAppBar() = with(binding) {
        topAppBar.setNavigationOnClickListener {
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
                    viewModel.onConfirmChanges()
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

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collectLatest { updateUiState(it) } }
                launch { viewModel.uiEvent.collect { observeUiEvents(it) } }
            }
        }
    }

    private fun updateUiState(state: EditBookingUiState) {
        binding.progressBar.isVisible = state.isLoading
        binding.bookButton.isEnabled =
            state.selectedDate != null && state.selectedTimeSlot != null && !state.isLoading

        state.selectedDate?.let {
            val calendar = Calendar.getInstance().apply {
                set(it.year, it.monthValue - 1, it.dayOfMonth)
            }
            binding.calendarView.date = calendar.timeInMillis
        }

        binding.timeSlotsChipGroup.apply {
            state.selectedTimeSlot?.let { selectedTimeSlot ->
                for (i in 0 until childCount) {
                    val chip = getChildAt(i) as Chip
                    if (chip.text == DateTimeUtils.formatTime(selectedTimeSlot.startTime)) {
                        chip.isChecked = true
                        break
                    }
                }
            } ?: run {
                clearCheck()
            }
        }

        state.booking?.let {
            binding.serviceNameTextView.text = it.serviceName
        }

        binding.timeSlotsChipGroup.apply {
            removeAllViews()
            state.availableTimeSlots.forEach { timeSlot ->
                addView(createTimeSlotChip(timeSlot))
            }
        }
    }

    private fun observeUiEvents(event: EditBookingViewModel.EditBookingUiEvent) {
        when (event) {
            is EditBookingViewModel.EditBookingUiEvent.ShowError -> showSnackbar(event.message)
            is EditBookingViewModel.EditBookingUiEvent.BookingUpdatedSuccessfully -> {
                showSnackbar(UiText.StringResource(R.string.booking_updated))
                Log.d("EditBookingFragment", "Navigating up...") // Aggiunto log
                findNavController().popBackStack(R.id.bookingDetailFragment, false)

            }
        }
    }

    private fun createTimeSlotChip(timeSlot: TimeSlot): Chip {
        return Chip(requireContext()).apply {
            text = DateTimeUtils.formatTime(timeSlot.startTime)
            isCheckable = true
            setOnClickListener { viewModel.onTimeSlotSelected(timeSlot) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}