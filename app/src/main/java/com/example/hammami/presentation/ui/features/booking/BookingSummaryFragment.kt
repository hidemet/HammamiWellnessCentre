package com.example.hammami.presentation.ui.features.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hammami.databinding.FragmentBookingSummaryBinding
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class BookingSummaryFragment : BaseFragment() {

    private var _binding: FragmentBookingSummaryBinding? = null
    private val binding get() = _binding!!

    private val args: BookingSummaryFragmentArgs by navArgs()

    // Non è necessario un ViewModel separato, i dati sono già nel BookingViewModel
    private val viewModel: BookingViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        val booking = args.booking
        val serviceName = booking.serviceName
        val bookingDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(booking.date)
        val bookingTime = "${booking.startTime} - ${booking.endTime}"

        binding.serviceName.text = serviceName
        binding.bookingDate.text = bookingDate
        binding.bookingTime.text = bookingTime

        binding.goToHomeButton.setOnClickListener {
            findNavController().navigate(BookingSummaryFragmentDirections.actionBookingSummaryFragmentToHomeFragment())
        }
    }

    override fun observeFlows() {
        // Non è necessario osservare flussi in questo Fragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}