package com.example.hammami.presentation.ui.features.service

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.hammami.R
import com.example.hammami.databinding.ServizioDettaglioBinding
import com.example.hammami.domain.usecase.GetReviewsUseCase
import com.example.hammami.presentation.ui.adapters.BeneficiAdapter
import com.example.hammami.presentation.ui.adapters.Beneficio
import com.example.hammami.presentation.ui.adapters.ReviewsAdapter
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ServiceDetailFragment : BaseFragment() {

    private var _binding: ServizioDettaglioBinding? = null
    private val binding get() = _binding!!
    private val args: ServiceDetailFragmentArgs by navArgs()

    //private lateinit var serviceDetailAdapter: ServiceDetailAdapter

    private lateinit var beneficiAdapter: BeneficiAdapter

    //private val viewModel: ServiceDetailViewModel by viewModels()
    //private val viewModelReviews: ReviewsViewModel by viewModels()

    private val serviceDetailViewModel: ServiceDetailViewModel by viewModels()
    private val reviewsViewModel: ReviewsViewModel by viewModels()

    private lateinit var reviewsAdapter: ReviewsAdapter

    @Inject
    lateinit var getReviewsUseCase: GetReviewsUseCase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ServizioDettaglioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val service = args.service
        //val viewModelFactory = ServiceDetailViewModelFactory(getReviewsUseCase, service)
        //val viewModel: ServiceDetailViewModel by viewModels { viewModelFactory }
        //viewModel.loadData(service)
        serviceDetailViewModel.setService(service)
        reviewsViewModel.loadReviewsData(service)
        setupUI()
        observeFlows()

        /*
        val service = args.service
        //val viewModelFactory = ServiceDetailViewModelFactory(getReviewsUseCase, service)
        //viewModel = ViewModelProvider(this, viewModelFactory).get(ServiceDetailViewModel::class.java)
        viewModel.setService(service)
        setupUI()
        observeFlows()

         */

    }

    override fun setupUI() {
        setupAppBar()
        setupInfo()
        setupRecyclerView()
        //setupInitialState()
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeReviews() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeReviews() {
        reviewsViewModel.allReviews.collectLatest { state ->
            //updateUI(state)
            reviewsAdapter.submitList(state)
        }
    }

    private suspend fun observeEvents() {
        binding.buttonProsegui.setOnClickListener {
            /*
            val serviceId = args.service.id
            val serviceDuration = args.service.length
            val serviceName = args.service.name
            val servicePrice = args.service.price
            val action = ServiceDetailFragmentDirections.actionServiceDetailFragmentToBookingFragment(serviceId,
                serviceDuration!!.toFloat(), serviceName,
                servicePrice!!
            )
            findNavController().navigate(action)
        }

             */

            val serviceId = args.service.id
            val serviceDuration = args.service.length?.toFloat() ?: 0.0f
            val serviceName = args.service.name
            val servicePrice = args.service.price ?: 0.0f
            val action =
                ServiceDetailFragmentDirections.actionServiceDetailFragmentToBookingFragment(
                    serviceId, serviceDuration, serviceName, servicePrice
                )
            findNavController().navigate(action)
        }
    }

    /*

    private fun updateUI(state: ServiceDetailViewModel.ServiceReviews) = with(binding) {
        //progressIndicator.isVisible = state.isLoading
        updateReviewsList(state)
    }

    private fun updateReviewsList(state: ServiceDetailViewModel.ServiceReviews) = with(binding) {
        reviewsAdapter.submitList(state.serviceReviews)
    }

     */

    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    private fun handleEvent(event: ServiceDetailViewModel.UiEvent) {
        when (event) {
            is ServiceDetailViewModel.UiEvent.ShowMessage -> showSnackbar(event.message)
            is ServiceDetailViewModel.UiEvent.ShowError -> showSnackbar(event.message)
            else -> {}
        }
    }

    private fun setupInfo(){
        //serviceDetailAdapter = ServiceDetailAdapter()
        //beneficiAdapter = BeneficiAdapter()

        if (args.service.image != null) {
            Glide.with(binding.root).load(args.service.image).into(binding.ivServizio)
        } else {
            // Carica un'immagine placeholder o nascondi l'ImageView
            binding.ivServizio.setImageResource(R.drawable.placeholder_image)
        }

        //serviceDetailAdapter.submitList(listOf(args.service))
        //binding.apply {
        binding.tvTitolo.text = args.service.name
        binding.tvContenutoDescrizione.text = args.service.description
        binding.tvContenutoDurata.text = "${args.service.length} min"
        binding.tvContenutoPrezzo.text = "${args.service.price} €"

        /*
        beneficiList?.forEach { beneficio ->
            Log.e("ServiceDetailViewModel", "${beneficio.descrizione}")
        }


         */
        /*
            if (args.service.benefits != null) {
                //ContenutoBenefici.text = args.service.benefits
                binding.rvContenutoBenefici.apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    adapter = beneficiAdapter
                }
            } else {
                Log.e("ServiceDetailFragment", "Benefits is null")
                binding.tvTitoloBenefici.visibility = View.GONE
                binding.rvContenutoBenefici.visibility = View.GONE
            }
        //}

         */

        val beneficiList = args.service.benefits?.split("*")?.map { Beneficio(it) } ?: emptyList()

        //beneficiAdapter = BeneficiAdapter(beneficiList)

        if(beneficiList.isNotEmpty()){
            beneficiAdapter = BeneficiAdapter(beneficiList)
            binding.rvContenutoBenefici.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = beneficiAdapter
            }
            beneficiAdapter.submitList(beneficiList)
        }else{
            Log.e("ServiceDetailFragment", "Benefits is null")
            binding.tvTitoloBenefici.visibility = View.GONE
            binding.rvContenutoBenefici.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        //serviceDetailAdapter = ServiceDetailAdapter()
        reviewsAdapter = ReviewsAdapter()

        if(args.service.reviews == null){
            binding.tvContenutoRecensioni.visibility = View.VISIBLE
            binding.rvRecensioni.visibility = View.GONE
        }else {
            //reviewsAdapter = ReviewsAdapter()
            binding.rvRecensioni.apply {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                adapter = reviewsAdapter
            }
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
        Log.d("BenessereFragment", "onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}