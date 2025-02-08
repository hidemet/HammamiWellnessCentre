package com.example.hammami.presentation.ui.features.admin.booking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.core.time.DateTimeUtils.formatDate
import com.example.hammami.core.time.DateTimeUtils.formatTimeRange
import com.example.hammami.databinding.ItemBookingBinding
import com.example.hammami.domain.model.Booking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingsAdapter(private val onBookingClick: (String) -> Unit) :
    ListAdapter<Booking, BookingsAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = getItem(position)
        holder.bind(booking)
        holder.itemView.setOnClickListener { onBookingClick(booking.id) }
    }

    inner class BookingViewHolder(private val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.serviceName.text = booking.serviceName
            binding.bookingDate.text = formatDate(booking.startDate)
            binding.bookingTime.text = formatTimeRange(booking.startDate, booking.endDate)
        }
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem == newItem
        }
    }
}