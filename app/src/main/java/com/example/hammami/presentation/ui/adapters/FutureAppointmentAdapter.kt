package com.example.hammami.presentation.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.core.time.DateTimeUtils
import com.example.hammami.databinding.ItemAppuntamentoFuturoBinding
import com.example.hammami.domain.model.Booking
import com.example.hammami.presentation.ui.features.client.AppointmentsFragmentDirections // Assicurati che sia il percorso corretto

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
        val booking = getItem(position)
        Log.d("FutureAppointmentAdapter", "onBindViewHolder: posizione=$position, bookingId=${booking.id}") // LOG
        holder.bind(booking)
    }

    inner class ViewHolder(
        private val binding: ItemAppuntamentoFuturoBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(appointment: Booking) {
            with(binding) {
                setupAppointmentInfo(appointment)
                setupClickListener(appointment)
            }
        }
        private fun ItemAppuntamentoFuturoBinding.setupAppointmentInfo(appointment: Booking) {
            tvTitle.text = appointment.serviceName
            binding.tvGiorno.text = DateTimeUtils.formatDate(appointment.startDate)
            binding.tvOra.text = DateTimeUtils.formatTimeRange(appointment.startDate, appointment.endDate)
        }
        private fun ItemAppuntamentoFuturoBinding.setupClickListener(booking: Booking) {
            root.setOnClickListener {
                val action = AppointmentsFragmentDirections.actionAppointmentsFragmentToBookingDetailFragment(booking.id)
                it.findNavController().navigate(action)
            }
        }
    }

}

class AppointmentDiffCallback : DiffUtil.ItemCallback<Booking>() {
    override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean {
        val areTheSame = oldItem.id == newItem.id
        Log.d(
            "AppointmentDiffCallback",
            "areItemsTheSame: oldId=${oldItem.id}, newId=${newItem.id}, result=$areTheSame"
        )
        return areTheSame
    }

    override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean {
        val areTheSame = oldItem == newItem
        Log.d(
            "AppointmentDiffCallback",
            "areContentsTheSame: oldItem=$oldItem, newItem=$newItem, result=$areTheSame"
        )
        return areTheSame
    }
}