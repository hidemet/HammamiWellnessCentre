package com.example.hammami.presentation.ui.features.admin.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.R
import androidx.navigation.fragment.findNavController
import com.example.hammami.databinding.FragmentHomeAdminBinding
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.admin.booking.BookingsAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HomeAdminFragment : BaseFragment() {

    private val viewModel: HomeAdminViewModel by viewModels()
    private var _binding: FragmentHomeAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookingsAdapter: BookingsAdapter // Il tuo adapter per la RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAdminBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun setupUI() { // Implementa il metodo astratto di BaseFragment
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        bookingsAdapter = BookingsAdapter(onBookingClick = { bookingId ->
            val action = HomeAdminFragmentDirections.actionHomeAdminFragmentToBookingDetailFragment(bookingId)
            findNavController().navigate(action)
        })
        binding.bookingsRecyclerView.apply {
            adapter = bookingsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun observeFlows() { // Implementa il metodo astratto di BaseFragment
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collectLatest { state ->
                        binding.linearProgressIndicator.isVisible = state.isLoading
                        bookingsAdapter.submitList(state.bookings)
                    }
                }
                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is HomeAdminViewModel.UiEvent.ShowSnackbar -> {
                                showSnackbar(event.message) // Usa il metodo di BaseFragment
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