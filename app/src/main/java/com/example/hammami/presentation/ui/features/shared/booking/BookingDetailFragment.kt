package com.example.hammami.presentation.ui.features.shared.booking

import android.content.Intent
import android.net.Uri
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
import com.example.hammami.core.utils.WellnessCenterInfo
import com.example.hammami.databinding.FragmentBookingDetailBinding
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.User
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.shared.booking.BookingDetailViewModel.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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
        viewModel.loadBooking(args.bookingId)
        setupUI()
        observeFlows()
    }

    override fun setupUI() {
        setupAppBar()
        setupOptionsList()

    }

    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    private fun setupOptionsList() {
        optionsAdapter = BookingOptionsAdapter(emptyList()) { optionType ->
            viewModel.onOptionSelected(optionType)
        }
        binding.optionsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = optionsAdapter
        }

        // Osserva i cambiamenti *nello stato* e aggiorna l'adapter.
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->  // Usa collectLatest per lo stato
                optionsAdapter.submitList(state.availableOptions)
            }
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
                launch { viewModel.state.collectLatest { state -> updateUI(state) } }
                launch { viewModel.uiEvent.collect { event -> observeUiEvent(event) } }
            }
        }
    }

    private fun startNavigation() {
        val address = WellnessCenterInfo.ADDRESS
        val navigationIntentUri = Uri.parse("google.navigation:q=${Uri.encode(address)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, navigationIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
            return
        }

        // 2. Se Google Maps non è disponibile, prova con un intent generico di geo URI (ricerca)
        val geoIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
        val geoIntent = Intent(Intent.ACTION_VIEW, geoIntentUri)
        if (geoIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(geoIntent)
            return
        }


        // 3. Fallback: Nessuna app di navigazione trovata
        showSnackbar(UiText.StringResource(R.string.error_navigation_app_not_found))
    }

    private fun updateUI(state: BookingDetailUiState) {
        state.booking?.let { booking ->
            state.user?.let { user ->
                updateBookingDetails(booking, user)
            }
        }
        optionsAdapter.submitList(state.availableOptions)
        showLoading(state.isLoading)
    }

    private fun observeUiEvent(event: BookingDetailUiEvent) {
        when (event) {
            is BookingDetailUiEvent.ShowError -> showSnackbar(event.message)
            is BookingDetailUiEvent.BookingCancelled -> {
                showSnackbar(UiText.StringResource(R.string.booking_cancelled))
                findNavController().popBackStack()
            }

            is BookingDetailUiEvent.NavigateToEditBooking -> navigateToEditBooking()
            is BookingDetailUiEvent.ConfirmCancellation -> showCancellationConfirmationDialog(
                event.bookingId
            )

            is BookingDetailUiEvent.StartNavigation -> startNavigation()
        }
    }

    private fun updateBookingDetails(booking: Booking, user: User) = with(binding) {
        bookingCard.serviceName.text = booking.serviceName
        bookingCard.bookingDate.text = formatDate(booking.startDate)
        bookingCard.bookingTime.text = formatTimeRange(booking.startDate, booking.endDate)
        bookingCard.bookingPrice.text = getString(R.string.booking_price_format, booking.price)
        bookingCard.bookingPriceLayout.visibility = View.VISIBLE
        bookingCard.bookingClientLayout.visibility = View.VISIBLE
        bookingCard.bookingClientLayout.visibility =
            if (viewModel.isAdmin) View.VISIBLE else View.GONE
        bookingCard.bookingClient.text =
            getString(R.string.client_name_format, user.firstName, user.lastName)
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