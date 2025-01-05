package com.example.hammami.presentation.ui.features.appointments

//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.repeatOnLifecycle
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.hammami.R
//import com.example.hammami.databinding.FragmentBenessereBinding
//import com.example.hammami.databinding.FragmentNewAppointmentsBinding
//import com.example.hammami.databinding.FragmentPastAppointmentsBinding
//import com.example.hammami.presentation.ui.features.userProfile.UserProfileViewModel
//import com.example.hammami.presentation.ui.adapters.AppointmentAdapter
//import com.example.hammami.presentation.ui.adapters.BenessereAdapter
//import com.example.hammami.presentation.ui.adapters.OldAppointmentsAdapter
//import com.example.hammami.presentation.ui.features.BaseFragment
//import com.example.hammami.presentation.ui.features.service.BenessereFragment
//import com.example.hammami.presentation.ui.features.service.BenessereViewModel
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.launch
//
//@AndroidEntryPoint
//class PastAppointmentsFragment : BaseFragment(){
//
//    private lateinit var appointmentAdapter: OldAppointmentsAdapter
//
//    private var _binding: FragmentPastAppointmentsBinding? = null
//    private val binding get() = _binding!!
//
//    private val viewModel: AppointmentsViewModel by viewModels()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentPastAppointmentsBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        viewModel.loadPastAppointmentsData(viewModel.userEmail!!)
//        setupUI()
//        observeFlows()
//    }
//
//    override fun setupUI() {
//        //setupAppBar()
//        setupRecyclerView()
//    }
//
//    override fun observeFlows() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                launch { observeAppointments() }
//                //launch { observeEvents() }
//            }
//        }
//    }
//
//    private suspend fun observeAppointments() {
//        viewModel.pastAppointments.collectLatest { state ->
//            //updateUI(state)
//            appointmentAdapter.submitList(state)
//        }
//    }
//
//
//    private fun handleEvent(event: AppointmentsViewModel.UiEvent) {
//        when (event) {
//            is AppointmentsViewModel.UiEvent.ShowMessage -> showSnackbar(event.message)
//            is AppointmentsViewModel.UiEvent.ShowError -> showSnackbar(event.message)
//            else -> {}
//        }
//    }
//
//    private fun setupRecyclerView() {
//        appointmentAdapter = OldAppointmentsAdapter()
//        binding.rvPastAppointments.apply{
//            layoutManager =
//                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
//            adapter = appointmentAdapter
//        }
//    }

    /*
    override fun onResume() {
        super.onResume()
        Log.d("BenessereFragment", "onResume")
        viewModel.loadData()
    }

    override fun onPause() {
        super.onPause()
        Log.d("BenessereFragment", "onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

     */

//}



/*
override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
): View? {

    return inflater.inflate(R.layout.fragment_new_appointments, container, false)
}


 */