package com.example.hammami.presentation.ui.features.booking

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
import com.example.hammami.core.utils.TimeSlotCalculator
import com.example.hammami.databinding.FragmentBookingBinding
import com.example.hammami.domain.model.Service
import com.example.hammami.domain.model.payment.PaymentItem
import com.example.hammami.presentation.ui.features.BaseFragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class BookingFragment : BaseFragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookingViewModel by activityViewModels()
    private val args: BookingFragmentArgs by navArgs()

    private val service: Service by lazy { args.service }
    //  private var selectedDate: Date? = null
    // private var selectedTimeSlot: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        viewModel.onServiceChanged(service)
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
            is BookingViewModel.BookingUiEvent.BookingSuccess -> {}
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

        if (uiState.isBookingConfirmed) {
            Snackbar.make(binding.root, "Booking confirmed!", Snackbar.LENGTH_SHORT).show()
            viewModel.resetBookingConfirmation()
        }
    }

    private fun createTimeSlotChip(timeSlot: TimeSlotCalculator.AvailableSlot): Chip {
        return Chip(requireContext()).apply {
            text = "${timeSlot.startTime} - ${timeSlot.operatorId}"
            isCheckable = true
            setOnClickListener {
                viewModel.onTimeSlotSelected(timeSlot)
            }
        }
    }


//    private fun navigateToPayment(event: BookingViewModel.BookingUiEvent.NavigateToPayment) {
//        val paymentItem = PaymentItem.ServiceBookingPayment(
//            serviceName = event.service.name,
//            price = event.service.price?.toDouble() ?: 0.0,
//            bookingId = viewModel.uiState.value.currentBookingId ?: "",
//            date = viewModel.uiState.value.selectedDate,
//            startTime = viewModel.uiState.value.selectedTimeSlot ?: "",
//            duration = event.service.length?.toInt() ?: 0
//        )
//        findNavController().navigate(
//            BookingFragmentDirections.actionBookingFragmentToPaymentFragment(paymentItem))
//    }

    private fun navigateToPayment() {
        val paymentItem = PaymentItem.ServiceBookingPayment(
            serviceName = service.name,
            price = service.price?.toDouble() ?: 0.0,
            bookingId = viewModel.uiState.value.currentBookingId ?: "",
            date = viewModel.uiState.value.selectedDate,
            startTime = viewModel.uiState.value.selectedTimeSlot?.startTime ?: "",
            endTime = viewModel.uiState.value.selectedTimeSlot?.endTime ?: "",
            operatorId = viewModel.uiState.value.selectedTimeSlot?.operatorId
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
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(calendar.time)
            viewModel.onDateSelected(formattedDate)
        }

        binding.bookButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.reserveSlot()
            }
            navigateToPayment()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

//    private fun setupViews() {
//        binding.serviceNameTextView.text = service.name
//        binding.serviceDescriptionTextView.text = service.description
//        binding.selectDateButton.setOnClickListener {
//            showDatePicker()
//        }
//        binding.bookNowButton.setOnClickListener {
//            bookService()
//        }
//    }
//
//    private fun observeViewModel() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.availableTimeSlots.collect { result ->
//                    binding.timeSlotsChipGroup.removeAllViews()
//                    binding.timeSlotsProgressBar.visibility = View.GONE
//                    when (result) {
//                        is Result.Success -> {
//                            result.data.forEach { timeSlot ->
//                                val chip = com.google.android.material.chip.Chip(requireContext())
//                                chip.text = timeSlot
//                                chip.isCheckable = true
//                                chip.setOnClickListener {
//                                    selectedTimeSlot = timeSlot
//                                }
//                                binding.timeSlotsChipGroup.addView(chip)
//                            }
//                        }
//                        is Result.Error -> {
//                            Snackbar.make(binding.root, result.error.toString(), Snackbar.LENGTH_SHORT).show()
//                        }
//                        is Result.Loading -> {
//                            binding.timeSlotsProgressBar.visibility = View.VISIBLE
//                        }
//                    }
//                }
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.bookingResult.collect { result ->
//                    binding.bookingProgressBar.visibility = View.GONE
//                    when (result) {
//                        is Result.Success -> {
//                            Snackbar.make(binding.root, "Prenotazione effettuata con successo!", Snackbar.LENGTH_SHORT).show()
//                            // TODO: Navigate to confirmation or appointments screen
//                        }
//                        is Result.Error -> {
//                            Snackbar.make(binding.root, "Errore durante la prenotazione: ${result.error}", Snackbar.LENGTH_SHORT).show()
//                        }
//                        is Result.Loading -> {
//                            binding.bookingProgressBar.visibility = View.VISIBLE
//                        }
//                        null -> {
//                            // Do nothing, initial state
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun showDatePicker() {
//        val datePicker = MaterialDatePicker.Builder.datePicker()
//            .setTitleText("Seleziona la data")
//            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
//            .build()
//
//        datePicker.addOnPositiveButtonClickListener { selection ->
//            val calendar = Calendar.getInstance()
//            calendar.timeInMillis = selection
//            calendar.set(Calendar.HOUR_OF_DAY, 0)
//            calendar.set(Calendar.MINUTE, 0)
//            calendar.set(Calendar.SECOND, 0)
//            calendar.set(Calendar.MILLISECOND, 0)
//            selectedDate = calendar.time
//            binding.selectedDateTextView.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate!!)
//            selectedDate?.let {
//                viewModel.getAvailableTimeSlots(service, it)
//                binding.timeSlotsLabelTextView.visibility = View.VISIBLE
//            }
//        }
//        datePicker.show(childFragmentManager, "DATE_PICKER")
//    }
//
//    private fun bookService() {
//        if (selectedDate != null && selectedTimeSlot != null) {
//            // Assuming you have a way to get the current user ID
//            val userId = "USER_ID_EXAMPLE" // Replace with actual user ID retrieval
//            viewModel.scheduleBooking(service, selectedDate!!, selectedTimeSlot!!, userId)
//        } else {
//            Snackbar.make(binding.root, "Seleziona una data e un orario", Snackbar.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
