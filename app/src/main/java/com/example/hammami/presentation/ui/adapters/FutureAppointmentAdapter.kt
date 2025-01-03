package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.databinding.ItemAppuntamentoFuturoBinding
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.localDate
import com.example.hammami.presentation.ui.features.client.AppointmentsFragmentDirections
import java.time.format.DateTimeFormatter

class FutureAppointmentAdapter : ListAdapter<Booking, FutureAppointmentAdapter.ViewHolder>(AppointmentDiffCallback())  {
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
        fun bind(appointment: Booking) {
            with(binding) {
                setupAppointmentInfo(appointment)
                setupClickListener(appointment)
            }
        }
        private fun ItemAppuntamentoFuturoBinding.setupAppointmentInfo(appointment: Booking) {
            /*
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
             */

            tvTitle.text = appointment.serviceName
            binding.tvGiorno.text = appointment.localDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Data non disponibile"
            binding.tvOra.text = "${appointment.startTime} - ${appointment.endTime}"


        }
        private fun ItemAppuntamentoFuturoBinding.setupClickListener(appointment: Booking) {
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
class AppointmentDiffCallback : DiffUtil.ItemCallback<Booking>() {
    override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean =
        oldItem.transactionId == newItem.transactionId
    override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean =
        oldItem == newItem
}