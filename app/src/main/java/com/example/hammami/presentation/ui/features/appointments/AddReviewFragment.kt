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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.hammami.R
import com.example.hammami.data.datasource.reviews.FirebaseFirestoreReviewsDataSource
import com.example.hammami.databinding.FragmentAddReviewBinding
import com.example.hammami.databinding.FragmentPastAppointmentsBinding
import com.example.hammami.databinding.ServizioDettaglioBinding
import com.example.hammami.domain.model.Review
import com.example.hammami.domain.usecase.GetCurrentUserDataUseCase
import com.example.hammami.domain.usecase.GetReviewsUseCase
import com.example.hammami.domain.usecase.SetReviewUseCase
import com.example.hammami.presentation.ui.adapters.AddReviewAdapter
//import com.example.hammami.presentation.ui.adapters.AppointmentAdapter
import com.example.hammami.presentation.ui.adapters.BeneficiAdapter
import com.example.hammami.presentation.ui.adapters.Beneficio
import com.example.hammami.presentation.ui.adapters.ReviewsAdapter
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.service.ReviewsViewModel
import com.example.hammami.presentation.ui.features.service.ServiceDetailFragmentArgs
import com.example.hammami.presentation.ui.features.service.ServiceDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.hammami.core.result.Result
import com.example.hammami.domain.usecase.AddReviewToServiceUseCase
import com.example.hammami.domain.usecase.GetCollectionFromServiceIdUseCase
import com.example.hammami.domain.usecase.GetIdFromNameUseCase
import com.example.hammami.domain.usecase.booking.UpdateBookingReviewUseCase
import com.example.hammami.presentation.ui.features.service.BenessereFragmentDirections

@AndroidEntryPoint
class AddReviewFragment : BaseFragment() {

    private var _binding: FragmentAddReviewBinding? = null
    private val binding get() = _binding!!
    private val args: AddReviewFragmentArgs by navArgs()

    private val viewModel: AppointmentsViewModel by viewModels()

    private lateinit var reviewsAdapter: ReviewsAdapter

    @Inject
    lateinit var getCollectionFromServiceIdUseCase: GetCollectionFromServiceIdUseCase

    @Inject
    lateinit var getIdFromNameUseCase: GetIdFromNameUseCase

    @Inject
    lateinit var addReviewToServiceUseCase: AddReviewToServiceUseCase

    @Inject
    lateinit var getReviewsUseCase: GetReviewsUseCase

    @Inject
    lateinit var setReviewUseCase: SetReviewUseCase

    @Inject
    lateinit var updateBookingReviewUseCase: UpdateBookingReviewUseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val appointmentService = args.service
        setupUI()
        observeFlows()
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

    /*
    private suspend fun observeEvents() {
        binding.btnInvia.setOnClickListener(){
            var reviewId = ""
            val textReview = binding.editTextReview.text.toString()
            val rating = binding.ratingBar.rating
            val reviewToAdd =
                viewModel.uiState.value.user?.let { it1 -> Review(textReview, it1.firstName, rating) }
            viewLifecycleOwner.lifecycleScope.launch {
                if (reviewToAdd != null) {
                    reviewId = setReviewUseCase(reviewToAdd)
                }
            }

            //AGGIUNGERE FEEDBACK ALL'UTENTE DELL'AVVENUTO INVIO DELLA RECENSIONE E POI FARLO TORNARE ALLA PAGINA PRECEDENTE

            //onBackClick()
        }
    }
     */

    private suspend fun observeEvents() {
        binding.btnInvia.setOnClickListener {
            //val nomeServizio = args.appointment.name          <------------------------------------ PRIMA NON ERA COMMENTATO DAVA ERRORE, VERIFICARE QUANDO TUTTO FUNZIONA
            val textReview = binding.editTextReview.text.toString()
            val rating = binding.ratingBar.rating
            val reviewToAdd = viewModel.uiState.value.user?.let { it1 ->
                Review(
                    textReview,
                    it1.firstName,
                    rating
                )
            }

            if (textReview.isEmpty()) {
                //showAlert("Per favore, inserisci una recensione.")
                binding.editTextReview.error = "Per favore, inserisci una recensione."
                return@setOnClickListener
            }

            if (rating == 0.0f) {
                //showAlert("Per favore, inserisci un voto.")
                //binding.ratingBar.error = "Per favore, inserisci un voto."
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                if (reviewToAdd != null) {
                    if (!args.appointment.hasReview) {
                        when (val result = setReviewUseCase(reviewToAdd)) {
                            is Result.Success -> {
                                val reviewId = result.data.first
                                when (val result2 =
                                    getIdFromNameUseCase(args.appointment.serviceName)) {
                                    is Result.Success -> {
                                        val serviceId = result2.data.toString()
                                        //Log.e("AddReview", "Nome servizio: $nomeServizio")
                                        //Log.e("AddReview", "serviceId: $serviceId")
                                        addReviewToServiceUseCase(serviceId, reviewId)
                                        updateBookingReviewUseCase(args.appointment.id)
                                        Log.e("AddReview", "Aggiornato il valore hasReview a true a seguito dell'aggiunta della recensione")

                                        //Log.e("AddReview", "Recensione aggiunta al servizio")
                                        val action =
                                            AddReviewFragmentDirections.actionAddReviewFragmentToReviewSummaryFragment(
                                                args.appointment.serviceName,
                                                reviewToAdd
                                            )
                                        it.findNavController().navigate(action)
                                    }

                                    is Result.Error -> {
                                        // Gestisci l'errore
                                    }
                                }
                            }

                            is Result.Error -> {
                                // Gestisci l'errore
                            }
                        }
                    }
                }
            }

        }
    }

        private suspend fun setRatingBar() {
            binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                binding.ratingBar.rating = rating
            }
        }

        /*
    override fun onResume() {
        super.onResume()
        Log.d("BenessereFragment", "onResume")
        viewModel.loadData()
    }
     */

        override fun onPause() {
            super.onPause()
            Log.d("AddReviewFragment", "onPause")
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

}