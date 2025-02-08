package com.example.hammami.presentation.ui.features.appointments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hammami.R
import com.example.hammami.databinding.FragmentConfirmReviewBinding
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReviewSummaryFragment : BaseFragment() {
    private var _binding: FragmentConfirmReviewBinding? = null
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
            findNavController().popBackStack(R.id.appointmentsFragment, false)
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