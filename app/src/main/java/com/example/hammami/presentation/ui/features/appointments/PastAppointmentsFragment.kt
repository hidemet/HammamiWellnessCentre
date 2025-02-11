package com.example.hammami.presentation.ui.features.appointments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.R
import com.example.hammami.databinding.FragmentPastAppointmentsBinding
import com.example.hammami.presentation.ui.adapters.PastAppointmentAdapter
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PastAppointmentsFragment : BaseFragment(){

    private lateinit var appointmentAdapter: PastAppointmentAdapter
    private val viewModel: AppointmentsViewModel by activityViewModels()
    private var _binding: FragmentPastAppointmentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPastAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeFlows()
    }

    override fun setupUI() {
        setupRecyclerView()
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeAppointments() }
                launch {  observeEvents() }
            }
        }
    }


    private suspend fun observeAppointments() {
        viewModel.state.collectLatest { state ->

            Log.d("PastAppointmentsFragment", "New state: ${state.pastAppointments.size} appointments") // LOG

            appointmentAdapter.submitList(state.pastAppointments) //Usa pastAppointments
        }
    }


    private suspend fun observeEvents() {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AppointmentsViewModel.UiEvent.ShowMessage -> showSnackbar(event.message)
                is AppointmentsViewModel.UiEvent.ShowError -> showSnackbar(event.message)
            }
        }
    }

    private fun setupRecyclerView() {
        appointmentAdapter = PastAppointmentAdapter()
        binding.rvPastAppointments.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = appointmentAdapter
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}