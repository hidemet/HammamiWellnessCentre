package com.example.hammami.fragments.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.databinding.ServizioDettaglioBinding
import com.example.hammami.viewmodel.ServizioViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ServiceDetailFragment : Fragment() {

    private lateinit var binding: ServizioDettaglioBinding
    // private val viewModel: ServizioViewModel by viewModels() // per ora tolgo perchè non serve poi vediamo
    private val args by navArgs<ServiceDetailFragmentArgs>()

    private val viewPagerAdapter by lazy { ViePager2Images() }
    private val reviewsAdapter by lazy { ColorsAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ServizioDettaglioBinding.inflate(inflater) //tolgo container e false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val service = args.service

        setupReviewsRV()
        setupViewPager()

        binding.apply {
            tvTitolo.text = service.name
            tvContenutoDescrizione.text = service.description
            tvContenutoDurata.text = "${service.length} h"
            tvContenutoPrezzo.text = "${service.price} €"
        }

        viewPagerAdapter.differ.submitList(service.images)
        service.reviews?.let { reviewsAdapter.differ.submitList(it) }


        /*
        val serviceId = args.serviceId
        viewModel.getServiceById(serviceId).observe(viewLifecycleOwner) { service ->
            service?.let {
                binding.tvTitolo.text = it.name
                binding.tvContenutoDescrizione.text = it.description
                binding.tvContenutoDurata.text = "${it.length} h"
                binding.tvContenutoPrezzo.text = "${it.price} €"
            }
        }

         */
    }

    private fun setupViewPager() {
        binding.apply {
            viewPagerProductImages.adapter = viewPagerAdapter
        }
    }

    private fun setupReviewsRV() {
        binding.rvRecensioni.apply {
            adapter = reviewsAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }
}