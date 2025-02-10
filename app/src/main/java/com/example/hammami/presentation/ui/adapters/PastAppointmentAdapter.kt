package com.example.hammami.presentation.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.core.time.DateTimeUtils
import com.example.hammami.core.ui.UiText
import com.example.hammami.databinding.ItemAppuntamentoPassatoBinding
import com.example.hammami.domain.model.Booking
import com.example.hammami.presentation.ui.features.client.AppointmentsFragmentDirections
import com.google.android.material.snackbar.Snackbar

class PastAppointmentAdapter :
    ListAdapter<Booking, PastAppointmentAdapter.ViewHolder>(PastAppointmentDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppuntamentoPassatoBinding.inflate(
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
            binding.tvGiorno.text = DateTimeUtils.formatDate(appointment.startDate)
            binding.tvOra.text =
                DateTimeUtils.formatTimeRange(appointment.startDate, appointment.endDate)
            Log.e("PastAppointmentAdapter", "hasReview: ${appointment.hasReview}")
            if (appointment.hasReview) {
                binding.ivAddReview.visibility = android.view.View.GONE
            }
        }

        private fun ItemAppuntamentoPassatoBinding.setupClickListener(appointment: Booking) {
            root.setOnClickListener {
                if (appointment.hasReview) {
                    Log.e("PastAppointmentAdapter", "L'utente ha già recensito questo appuntamento")
                    showSnackbar(UiText.StringResource(R.string.review_already_added))
                } else {
                    val action =
                        AppointmentsFragmentDirections.actionAppointmentsFragmentToAddReviewFragment(
                            appointment
                        )
                    it.findNavController().navigate(action)
                }
            }
        }

        private fun showSnackbar(message: UiText) {
            Snackbar.make(
                binding.root,
                message.asString(binding.root.context),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}

class PastAppointmentDiffCallback : DiffUtil.ItemCallback<Booking>() {
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