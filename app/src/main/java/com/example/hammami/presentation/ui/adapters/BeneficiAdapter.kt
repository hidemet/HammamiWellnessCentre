package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.databinding.ItemBeneficiBinding

data class Beneficio(
    val descrizione: String
)

class BeneficiAdapter(private val beneficiList: List<Beneficio>) : ListAdapter<Beneficio, BeneficiAdapter.ViewHolder>(BeneficiDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBeneficiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val beneficio = getItem(position) // Get the Beneficio object
        holder.bind(beneficio) // Use the 'descrizione' property
    }

    class ViewHolder(
        private val binding: ItemBeneficiBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(beneficio: Beneficio) {
            binding.tvBenefici.text = beneficio.descrizione
        }
    }

    private class BeneficiDiffCallback : DiffUtil.ItemCallback<Beneficio>() {

        override fun areContentsTheSame(oldItem: Beneficio, newItem: Beneficio): Boolean {
            return oldItem.descrizione == newItem.descrizione
        }

        override fun areItemsTheSame(oldItem: Beneficio, newItem: Beneficio): Boolean {
            return oldItem == newItem
        }
    }
}

