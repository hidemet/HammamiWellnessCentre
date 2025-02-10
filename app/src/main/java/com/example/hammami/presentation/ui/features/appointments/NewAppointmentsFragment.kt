package com.example.hammami.presentation.ui.features.appointments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.databinding.FragmentNewAppointmentsBinding
import com.example.hammami.presentation.ui.adapters.FutureAppointmentAdapter
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewAppointmentsFragment : BaseFragment(){

    private lateinit var appointmentAdapter: FutureAppointmentAdapter

    private var _binding: FragmentNewAppointmentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppointmentsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeFlows()
    }

    override fun setupUI() {
        //setupAppBar()
        setupRecyclerView()
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeAppointments() }
                //launch { observeEvents() }
            }
        }
    }

    private suspend fun observeAppointments() {
        viewModel.newAppointments.collect { state ->  // Usa collect
            appointmentAdapter.submitList(state)
        }
    }

    private fun handleEvent(event: AppointmentsViewModel.UiEvent) {
        when (event) {
            is AppointmentsViewModel.UiEvent.ShowMessage -> showSnackbar(event.message)
            is AppointmentsViewModel.UiEvent.ShowError -> showSnackbar(event.message)
            else -> {}
        }
    }

    private fun setupRecyclerView() {
        appointmentAdapter = FutureAppointmentAdapter()
        binding.rvNewAppointments.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = appointmentAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}