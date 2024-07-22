package com.example.hammami.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.models.ItemProfileOption


class ProfileOptionAdapter(private val options: List<ItemProfileOption>) :
    RecyclerView.Adapter<ProfileOptionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val leadingIcon: ImageView = view.findViewById(R.id.leadingIcon)
        val title: TextView = view.findViewById(R.id.optionTitle)
        val trailingIcon: ImageView = view.findViewById(R.id.trailingIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.leadingIcon.setImageResource(option.leadingIconResId)
        holder.title.text = option.title
        holder.trailingIcon.setImageResource(option.trailingIconResId)
        holder.itemView.setOnClickListener {
            it.findNavController().navigate(option.navigationDestination)
        }
    }

    override fun getItemCount() = options.size
}