package com.example.hammami.presentation.ui.features.booking

import android.os.Bundle
import android.os.Parcel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.databinding.FragmentBookingBinding
import com.example.hammami.presentation.ui.features.BaseFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone

@AndroidEntryPoint
class BookingFragment : BaseFragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookingViewModel by viewModels()
    private val args: BookingFragmentArgs by navArgs()
    private lateinit var timeSlotAdapter: TimeSlotAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        setupDatePicker()
        setupTimeSlotsList()

        viewModel.initializeBooking(
            serviceId = args.serviceId,
            serviceDuration = args.serviceDuration,
            serviceName = args.serviceName,
            servicePrice = args.servicePrice
        )
    }

    override fun observeFlows() {
        viewModel.state.collectLatestLifecycleFlow { state ->
            binding.progressBar.isVisible = state.isLoading
            timeSlotAdapter.updateSlots(state.availableSlots)

            state.selectedDate?.let { date ->
                binding.selectedDateText.text = date.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
            }
        }

        viewModel.uiEvent.collectLatestLifecycleFlow { event ->
            when (event) {
                is BookingViewModel.UiEvent.ShowError -> {
                    showSnackbar(event.message)
                }
                is BookingViewModel.UiEvent.NavigateToPayment -> {
                    findNavController().navigate(
                        BookingFragmentDirections
                            .actionBookingFragmentToPaymentFragment(event.paymentItem)
                    )
                }
            }
        }
    }

    private fun setupDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(CompositeDateValidator.allOf(listOf(
                DateValidatorPointForward.now(),
                object : CalendarConstraints.DateValidator {
                    override fun isValid(date: Long): Boolean {
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = date
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        return dayOfWeek in (Calendar.TUESDAY..Calendar.SATURDAY)
                    }

                    override fun writeToParcel(dest: Parcel, flags: Int) {}
                    override fun describeContents(): Int = 0
                }
            )))

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Seleziona data")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(constraintsBuilder.build())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val localDate = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            viewModel.onDateSelected(localDate)
        }

        binding.selectedDateText.setOnClickListener {
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }
    }

    private fun setupTimeSlotsList() {
        timeSlotAdapter = TimeSlotAdapter { slot ->
            viewModel.onSlotSelected(slot)
        }

        binding.timeSlotsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timeSlotAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}