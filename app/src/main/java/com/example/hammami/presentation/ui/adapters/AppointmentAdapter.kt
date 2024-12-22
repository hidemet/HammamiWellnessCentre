package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.databinding.ItemAppuntamentoFuturoBinding
import com.example.hammami.databinding.ItemAppuntamentoPassatoBinding
import com.example.hammami.domain.model.ServiceAppointment
import com.example.hammami.presentation.ui.features.client.AppointmentsFragmentDirections

class AppointmentAdapter : ListAdapter<ServiceAppointment, AppointmentAdapter.ViewHolder>(AppointmentDiffCallback())  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppuntamentoFuturoBinding.inflate(
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
        private val binding: ItemAppuntamentoFuturoBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: ServiceAppointment) {
            with(binding) {
                setupAppointmentInfo(appointment)
                setupClickListener(appointment)
            }
        }

        private fun ItemAppuntamentoFuturoBinding.setupAppointmentInfo(appointment: ServiceAppointment) {
            tvTitle.text = appointment.name

            if(appointment.day < 10){
                tvGiorno.text = "0" + appointment.day.toString() + "/"
                }else{
                    tvGiorno.text = appointment.day.toString() + "/"
                }

            if (appointment.month < 10){
                tvMese.text = "0" + appointment.month.toString()
                }else{
                    tvMese.text = appointment.month.toString()
                }

            if (appointment.hour < 10){
                tvOra.text = "0" + appointment.hour.toString() + ":"
                }else{
                    tvOra.text = appointment.hour.toString() + ":"
                }

            if (appointment.minute < 10){
                tvMinuto.text = "0" + appointment.minute.toString()
                }else{
                    tvMinuto.text = appointment.minute.toString()
                }
            }

        private fun ItemAppuntamentoFuturoBinding.setupClickListener(appointment: ServiceAppointment) {
            root.setOnClickListener {
                val action =
                    AppointmentsFragmentDirections.actionAppointmentsFragmentToAppointmentDetailFragment(
                        appointment
                    )
                it.findNavController().navigate(action)
            }

        }

        }

    }

    class AppointmentDiffCallback : DiffUtil.ItemCallback<ServiceAppointment>() {

        override fun areItemsTheSame(oldItem: ServiceAppointment, newItem: ServiceAppointment): Boolean =
            oldItem.email == newItem.email

        override fun areContentsTheSame(oldItem: ServiceAppointment, newItem: ServiceAppointment): Boolean =
            oldItem == newItem
    }
