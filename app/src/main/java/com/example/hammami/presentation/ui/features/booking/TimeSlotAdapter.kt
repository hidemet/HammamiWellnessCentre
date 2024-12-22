package com.example.hammami.presentation.ui.features.booking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.google.android.material.button.MaterialButton
import java.time.format.DateTimeFormatter

class TimeSlotAdapter(
    private val onSlotSelected: (BookingSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.SlotViewHolder>() {

    private var slots: List<BookingSlot> = emptyList()

    class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: MaterialButton = itemView.findViewById(R.id.slotButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val slot = slots[position]
        holder.button.text = "${slot.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - " +
                "${slot.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"

        holder.button.setOnClickListener { onSlotSelected(slot) }
    }

    override fun getItemCount() = slots.size

    fun updateSlots(newSlots: List<BookingSlot>) {
        slots = newSlots
        notifyDataSetChanged()
    }
}