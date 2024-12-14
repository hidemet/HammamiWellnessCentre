package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.databinding.FragmentAddReviewBinding
import com.example.hammami.databinding.ItemAppuntamentoPassatoBinding
import com.example.hammami.domain.model.ServiceAppointment
import com.example.hammami.presentation.ui.features.client.AppointmentsFragmentDirections

class AddReviewAdapter{}

    /*

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentAddReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: FragmentAddReviewBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: ServiceAppointment) {
            with(binding) {
                setupAppointmentInfo(appointment)
                setupClickListener(appointment)
            }
        }

        private fun FragmentAddReviewBinding.setupClickListener(appointment: ServiceAppointment) {
            root.setOnClickListener {
                val action =
                    AppointmentsFragmentDirections.actionAppointmentsFragmentToAddReviewFragment(
                        appointment
                    )
                it.findNavController().navigate(action)
            }

        }

    }

}

class OldAppointmentDiffCallback : DiffUtil.ItemCallback<ServiceAppointment>() {

    override fun areItemsTheSame(oldItem: ServiceAppointment, newItem: ServiceAppointment): Boolean =
        oldItem.email == newItem.email

    override fun areContentsTheSame(oldItem: ServiceAppointment, newItem: ServiceAppointment): Boolean =
        oldItem == newItem

     */