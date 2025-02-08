package com.example.hammami.presentation.ui.features.admin.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.R
import com.example.hammami.core.time.DateTimeUtils.formatDate
import com.example.hammami.core.time.DateTimeUtils.formatTimeRange
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.utils.BookingUiHelper
import com.example.hammami.databinding.FragmentBookingDetailBinding
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.ItemProfileOption
import com.example.hammami.domain.model.User
import com.example.hammami.presentation.ui.features.BaseFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookingDetailFragment : BaseFragment() {

    private var _binding: FragmentBookingDetailBinding? = null
    private val binding get() = _binding!!
    private val args: BookingDetailFragmentArgs by navArgs()

    private val viewModel: BookingDetailViewModel by viewModels()
    private lateinit var optionsAdapter: BookingOptionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeFlows()
        viewModel.loadBooking(args.bookingId)
    }

    override fun setupUI() {
        setupAppBar()
        setupOptionsList()
    }

    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    private fun setupOptionsList() {
        val options = listOf(
            ItemProfileOption(
                title = getString(R.string.reschedule_appointment),
                leadingIconResId = R.drawable.ic_event_repeat,
                action = { navigateToEditBooking() }
            ),
            ItemProfileOption(
                title = getString(R.string.cancel_booking),
                leadingIconResId = R.drawable.ic_delete,
                action = { showCancellationConfirmationDialog(args.bookingId) }
            )
        )

        optionsAdapter = BookingOptionsAdapter(options)
        binding.optionsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = optionsAdapter
        }
    }

    private fun navigateToEditBooking() {
        val action = BookingDetailFragmentDirections.actionBookingDetailFragmentToEditBookingFragment(args.bookingId)
        findNavController().navigate(action)
    }

    private fun showCancellationConfirmationDialog(bookingId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.cancel_booking_title)
            .setMessage(R.string.cancel_booking_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.confirm) { _, _ -> viewModel.cancelBooking(bookingId) }
            .show()
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect { state -> updateUI(state) } }
                launch { viewModel.uiEvent.collect { event -> observeUiEvent(event) } }
            }
        }
    }
    private fun updateUI(state: BookingDetailViewModel.BookingDetailUiState) {
        state.booking?.let { booking ->
            state.user?.let { user ->
                updateBookingDetails(booking, user)
            }
        }
        showLoading(state.isLoading)
    }

    private fun observeUiEvent(event: BookingDetailViewModel.BookingDetailUiEvent) {
        when (event) {
            is BookingDetailViewModel.BookingDetailUiEvent.ShowError -> showSnackbar(event.message)
            is BookingDetailViewModel.BookingDetailUiEvent.BookingCancelled -> {
                showSnackbar(UiText.StringResource(R.string.booking_cancelled))
                findNavController().popBackStack()
            }
        }
    }

    private fun updateBookingDetails(booking: Booking, user: User) = with(binding) {
        bookingCard.serviceName.text = booking.serviceName
        bookingCard.bookingDate.text = formatDate(booking.startDate)
        bookingCard.bookingTime.text = formatTimeRange(booking.startDate, booking.endDate)
        bookingCard.bookingPrice.text = getString(R.string.booking_price_format, booking.price)
        bookingCard.bookingPriceLayout.visibility = View.VISIBLE
        bookingCard.bookingClientLayout.visibility = View.VISIBLE
        bookingCard.bookingClient.text = getString(R.string.client_name_format, user.firstName, user.lastName)
        BookingUiHelper.setupBookingStatusChip(bookingStatusChip, booking.status, requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }
}