package com.example.hammami.presentation.ui.features.appointments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.hammami.databinding.FragmentAppointmentDetailBinding
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AppointmentDetailFragment : BaseFragment() {

    private val args: AppointmentDetailFragmentArgs by navArgs()

    private var _binding: FragmentAppointmentDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        //setupRecyclerView()
    }

    override fun setupUI() {
        setupAppBar()
        setupInfo()
    }

    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    private fun setupInfo() {
        binding.tvTitle.text = args.appointment.name

        if(args.appointment.day < 10){
            binding.tvGiorno.text = "0" + args.appointment.day.toString() + "/"
        }else{
            binding.tvGiorno.text = args.appointment.day.toString() + "/"
        }

        if (args.appointment.month < 10){
            binding.tvMese.text = "0" + args.appointment.month.toString()
        }else{
            binding.tvMese.text = args.appointment.month.toString()
        }

        if (args.appointment.hour < 10){
            binding.tvOra.text = "0" + args.appointment.hour.toString() + ":"
        }else{
            binding.tvOra.text = args.appointment.hour.toString() + ":"
        }

        if (args.appointment.minute < 10){
            binding.tvMinuto.text = "0" + args.appointment.minute.toString()
        }else{
            binding.tvMinuto.text = args.appointment.minute.toString()
        }
        binding.tvPrice.text = args.appointment.price.toString() + " €"
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeEvents() }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    //per ora nulla da aggiungere
                }
            }
        }
    }

