package com.example.hammami.presentation.ui.features.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hammami.R
import com.example.hammami.core.time.DateTimeUtils
import com.example.hammami.databinding.FragmentBookingSummaryBinding
import com.example.hammami.domain.model.Booking
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookingSummaryFragment : BaseFragment() {

    private var _binding: FragmentBookingSummaryBinding? = null
    private val viewModel: BookingViewModel by activityViewModels()
    private val binding get() = _binding!!

    private val args: BookingSummaryFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadBooking(args.bookingId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        setupTopAppBar()
        setupButtons()
    }

    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupButtons() = with(binding) {
        buttonGoToHome.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
        buttonViewBookings.setOnClickListener {
            findNavController().navigate(R.id.action_global_appointmentsFragment)
        }
    }

    private fun updateBookingUi(booking: Booking) {
        with(binding) {
            bookingCard.serviceName.text = booking.serviceName
            bookingCard.bookingDate.text = DateTimeUtils.formatDate(booking.startDate)
            bookingCard.bookingTime.text =
                DateTimeUtils.formatTimeRange(booking.startDate, booking.endDate)
            bookingCard.bookingPrice.isVisible = true
            bookingPrice.text = getString(R.string.booking_price_format, booking.price)
        }
        // Gestione click slla card
        //binding.bookingCard.setOnClickListener {
        // findNavController().navigate(R.id.action_bookingSummaryFragment_to_bookingDetailFragment, bundleOf("bookingId" to booking.id))
        //}
    }


    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeEvents() {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is BookingViewModel.BookingUiEvent.ShowError -> showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    private suspend fun observeState() {
        viewModel.newBooking.collect { booking -> booking?.let { updateBookingUi(it) } }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}