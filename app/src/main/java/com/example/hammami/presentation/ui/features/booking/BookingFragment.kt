package com.example.hammami.presentation.ui.features.booking

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hammami.core.utils.TimeSlotCalculator
import com.example.hammami.databinding.FragmentBookingBinding
import com.example.hammami.domain.model.Service
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.payment.PaymentViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class BookingFragment : BaseFragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private val args: BookingFragmentArgs by navArgs()
   private val service: Service by lazy { args.service }
    //  private var selectedDate: Date? = null
    // private var selectedTimeSlot: String? = null

    @Inject
    lateinit var bookingViewModelFactory: BookingViewModelFactory

    private val viewModel: BookingViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return bookingViewModelFactory.create(service) as T
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

    override fun setupUI() {
        setupTopAppBar()
        setupListeners()
        binding.serviceNameTextView.text = service.name
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { updateUiState(it) } }
                launch { viewModel.uiEvent.collect { observeUiEvents(it) } }
            }
        }
    }

    private fun observeUiEvents(event: BookingViewModel.BookingUiEvent) {
        when (event) {
            is BookingViewModel.BookingUiEvent.ShowError -> showSnackbar(event.message)
            is BookingViewModel.BookingUiEvent.ShowUserMassage -> showSnackbar(event.message)
            is BookingViewModel.BookingUiEvent.NavigateToPayment -> navigateToPayment(event.paymentItem)
        }
    }

    private fun updateUiState(uiState: BookingViewModel.BookingUiState) {
        binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
        binding.bookButton.isEnabled =
            uiState.selectedDate != null && uiState.selectedTimeSlot != null && !uiState.isLoading

        binding.timeSlotsChipGroup.apply {
            removeAllViews()
            uiState.availableTimeSlots.forEach { timeSlot ->
                addView(createTimeSlotChip(timeSlot))
            }

            // Seleziona la chip se un orario è già selezionato
            uiState.selectedTimeSlot?.let { selectedTimeSlot ->
                for (i in 0 until childCount) {
                    val chip = getChildAt(i) as Chip
                    if (chip.text == selectedTimeSlot.startTime) {
                        chip.isChecked = true
                        break
                    }
                }
            }

        }

        // Gestione click sulla chip
        binding.timeSlotsChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedChipId = checkedIds.first()
                val checkedChip = group.findViewById<Chip>(checkedChipId)
                val selectedTimeSlot = uiState.availableTimeSlots.find { it.startTime == checkedChip.text }
                    viewModel.onTimeSlotSelected(selectedTimeSlot)
            } else {
                viewModel.onTimeSlotSelected(null)
            }
        }

    }

    private fun createTimeSlotChip(timeSlot: TimeSlotCalculator.AvailableSlot): Chip {
        return Chip(requireContext()).apply {
            text = timeSlot.startTime
            isCheckable = true
            setOnClickListener {
                viewModel.onTimeSlotSelected(timeSlot)
            }
        }
    }

    private fun navigateToPayment(paymentItem: PaymentItem.ServiceBookingPayment) {
        Log.d("BookingFragment", "Navigating to PaymentFragment with bookingId: ${paymentItem.bookingId}")
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
        binding.calendarView.minDate = Calendar.getInstance().timeInMillis
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(calendar.time)
            Log.d("BookingFragment", "Data selezionata: $formattedDate")
            viewModel.onDateSelected(formattedDate)
        }

        binding.bookButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.onConfirmBooking()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}