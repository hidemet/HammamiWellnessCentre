package com.example.hammami.presentation.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.databinding.ItemAppuntamentoPassatoBinding
import com.example.hammami.domain.model.Booking
import com.example.hammami.domain.model.localDate
import com.example.hammami.presentation.ui.features.client.AppointmentsFragmentDirections
import java.time.format.DateTimeFormatter

class PastAppointmentAdapter : ListAdapter<Booking, PastAppointmentAdapter.ViewHolder>(PastAppointmentDiffCallback())  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppuntamentoPassatoBinding.inflate(
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
        private val binding: ItemAppuntamentoPassatoBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(appointment: Booking) {
            with(binding) {
                setupAppointmentInfo(appointment)
                setupClickListener(appointment)
            }
        }
        private fun ItemAppuntamentoPassatoBinding.setupAppointmentInfo(appointment: Booking) {
            tvTitle.text = appointment.serviceName
            binding.tvGiorno.text = appointment.localDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Data non disponibile"
            binding.tvOra.text = "${appointment.startTime} - ${appointment.endTime}"
            Log.e("PastAppointmentAdapter", "hasReview: ${appointment.hasReview}")
            if(appointment.hasReview){
                binding.ivAddReview.visibility = android.view.View.GONE
            }
        }
        private fun ItemAppuntamentoPassatoBinding.setupClickListener(appointment: Booking) {
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
class PastAppointmentDiffCallback : DiffUtil.ItemCallback<Booking>() {
    override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean =
        oldItem.transactionId == newItem.transactionId
    override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean =
        oldItem == newItem
}