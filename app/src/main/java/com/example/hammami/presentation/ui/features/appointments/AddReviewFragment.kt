package com.example.hammami.presentation.ui.features.appointments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddReviewFragment : BaseFragment() {

    private var _binding: FragmentAddReviewBinding? = null
    private val binding get() = _binding!!
    private val args: AddReviewFragmentArgs by navArgs()

    private val viewModel: AddReviewViewModel by viewModels()

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
        viewModel.loadUserData() //Carico il nome utente
    }

    override fun setupUI() {
        setupAppBar()
    }

    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeEvents() }
                launch { setRatingBar() }
            }
        }
    }


    private suspend fun observeEvents() {
        binding.btnInvia.setOnClickListener {
            val textReview = binding.editTextReview.text.toString()
            val rating = binding.ratingBar.rating
            val reviewToAdd = viewModel.uiState.value.user?.let { it1 ->
                Review(
                    textReview,
                    it1.firstName,
                    rating
                )
            }
            if (reviewToAdd != null) {
                viewModel.submitReview(reviewToAdd, args.appointment.serviceName, args.appointment)
            }
        }
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AddReviewViewModel.AddReviewUiEvent.ShowError -> {
                    showSnackbar(event.message)
                }

                is AddReviewViewModel.AddReviewUiEvent.ReviewAddedSuccessfully -> {
                    // Passa i dati necessari al ReviewSummaryFragment
                    val action =
                        AddReviewFragmentDirections.actionAddReviewFragmentToReviewSummaryFragment(
                            event.serviceName,
                            event.review
                        )
                    findNavController().navigate(action)
                }
            }
        }
    }

    private suspend fun setRatingBar() {
        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            binding.ratingBar.rating = rating
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}