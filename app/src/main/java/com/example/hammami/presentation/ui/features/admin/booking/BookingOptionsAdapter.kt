package com.example.hammami.presentation.ui.features.admin.booking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.domain.model.ItemProfileOption

class BookingOptionsAdapter(private val options: List<ItemProfileOption>) :
    RecyclerView.Adapter<BookingOptionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val leadingIcon: ImageView = view.findViewById(R.id.leadingIcon)
        val title: TextView = view.findViewById(R.id.optionTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.leadingIcon.setImageResource(option.leadingIconResId)
        holder.title.text = option.title
        holder.itemView.setOnClickListener {
            option.action.invoke()
        }
    }

    override fun getItemCount() = options.size
}