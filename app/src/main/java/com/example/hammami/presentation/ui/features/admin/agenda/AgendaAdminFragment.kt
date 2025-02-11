package com.example.hammami.presentation.ui.features.admin.agenda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.core.time.DateTimeUtils
import com.example.hammami.databinding.FragmentAgendaAdminBinding
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.admin.booking.BookingsAdapter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AgendaAdminFragment : BaseFragment() {

    private var _binding: FragmentAgendaAdminBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AgendaAdminViewModel by viewModels()
    private lateinit var bookingsAdapter: BookingsAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgendaAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeFlows()
    }

    override fun setupUI() {
        setupRecyclerView()
        setupDateRangePicker()
    }

    private fun setupRecyclerView() {
        bookingsAdapter = BookingsAdapter(onBookingClick = { bookingId ->
            val action =
                AgendaAdminFragmentDirections.actionAgendaAdminFragmentToBookingDetailFragment(
                    bookingId
                )
            findNavController().navigate(action)
        })
        binding.bookingsRecyclerView.apply {
            adapter = bookingsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }


    private fun setupDateRangePicker() {
        binding.dateRangeInput.setOnClickListener {
            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Seleziona l'intervallo di date")
                    .setSelection( //Imposto la selezione predefinita (settimana corrente)
                        Pair(
                            viewModel.state.value.selectedDateRange?.first
                                ?: MaterialDatePicker.thisMonthInUtcMilliseconds(),
                            viewModel.state.value.selectedDateRange?.second
                                ?: MaterialDatePicker.todayInUtcMilliseconds()
                        )
                    )
                    .build()


            dateRangePicker.addOnPositiveButtonClickListener { dateRange ->
                // Respond to positive button click.
                viewModel.onDateRangeSelected(dateRange.first, dateRange.second)
                //Formatto la visualizzazione
                val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedStartDate = dateFormatter.format(Date(dateRange.first))
                val formattedEndDate = dateFormatter.format(Date(dateRange.second))

                binding.dateRangeInput.setText(
                    DateTimeUtils.formatDateRange(
                        Date(dateRange.first),
                        Date(dateRange.second)
                    )
                )
            }
            dateRangePicker.show(parentFragmentManager, "date_range_picker")
        }
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collectLatest { state ->
                        binding.progressBar.isVisible =
                            state.isLoading  //Mostra/nasconde la progress bar
                        bookingsAdapter.submitList(state.bookings) //Aggiorna la lista

                        //Imposta il testo del text field, se abbiamo un range selezionato
                        state.selectedDateRange?.let { range ->
                            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val formattedStartDate = dateFormatter.format(Date(range.first))
                            val formattedEndDate = dateFormatter.format(Date(range.second))
                            binding.dateRangeInput.setText("$formattedStartDate - $formattedEndDate")
                        }
                    }
                }
                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is AgendaAdminViewModel.UiEvent.ShowSnackbar -> {
                                showSnackbar(event.message)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}