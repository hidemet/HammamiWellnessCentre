package com.example.hammami.presentation.ui.features.appointments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hammami.R
import com.example.hammami.databinding.FragmentBookingSummaryBinding
import com.example.hammami.databinding.FragmentConfirmReviewBinding
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.Review
import com.example.hammami.domain.model.localDate
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.booking.BookingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ReviewSummaryFragment : BaseFragment() {

    private var _binding: FragmentConfirmReviewBinding? = null
    //private val viewModel: BookingViewModel by activityViewModels()
    private val binding get() = _binding!!

    private val args: ReviewSummaryFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        updateReviewUi()
        setupTopAppBar()
        setupButtons()
    }

    override fun observeFlows() {
        //non serve
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
            //   findNavController().popBackStack(R.id.bookingListFragment, false)
            // TODO: implementare la navigazione alla lista delle prenotazioni
        }
    }

    private fun updateReviewUi() {

        binding.reviewCard.serviceName.text = args.serviceName
        binding.reviewCard.reviewRating.rating = args.review.valutazione.toFloat()
        binding.reviewCard.reviewDescription.text = args.review.commento

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}