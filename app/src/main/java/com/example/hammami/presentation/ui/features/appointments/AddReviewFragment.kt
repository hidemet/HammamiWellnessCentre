package com.example.hammami.presentation.ui.features.appointments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hammami.R
import com.example.hammami.databinding.FragmentAddReviewBinding
import com.example.hammami.domain.model.Review
import com.example.hammami.presentation.ui.adapters.ReviewsAdapter
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddReviewFragment : BaseFragment() {

    private var _binding: FragmentAddReviewBinding? = null
    private val binding get() = _binding!!
    private val args: AddReviewFragmentArgs by navArgs()

    private val viewModel: AppointmentsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeFlows()
    }

    override fun setupUI() {
        setupAppBar()
        setupSubmitButton()
    }

    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    private fun setupSubmitButton() {
        binding.btnInvia.setOnClickListener {
            val textReview = binding.editTextReview.text.toString()
            val rating = binding.ratingBar.rating

            lifecycleScope.launch {
                viewModel.submitReview(
                    reviewText = textReview,
                    rating = rating,
                    serviceName = args.appointment.serviceName,
                    booking = args.appointment
                )
            }
        }
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeState() {
        viewModel.state.collectLatest { state ->
            binding.editTextReview.error = state.textError?.asString(requireContext())
        }
    }

    private suspend fun observeEvents() {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AppointmentsViewModel.UiEvent.ShowError -> {
                    showSnackbar(event.message)
                }

                is AppointmentsViewModel.UiEvent.ShowMessage -> {
                    showSnackbar(event.message)
                    findNavController().popBackStack(R.id.appointmentsFragment, false)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}