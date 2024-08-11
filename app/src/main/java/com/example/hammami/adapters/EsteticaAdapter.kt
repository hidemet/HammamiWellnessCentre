package com.example.hammami.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hammami.R
import com.example.hammami.data.Service
import com.example.hammami.databinding.ItemEsteticaBinding
import com.example.hammami.databinding.ItemHomepageBinding
import com.example.hammami.databinding.ItemMassaggiBenessereBinding

class EsteticaAdapter : RecyclerView.Adapter<EsteticaAdapter.EsteticaViewHolder>() {

    inner class EsteticaViewHolder(private val binding: ItemEsteticaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service) {
            binding.apply {
                tvTitle.text = service.name
                tvPrice.text = "${service.price} €"
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Service>() {
        override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EsteticaViewHolder {
        return EsteticaViewHolder(
            ItemEsteticaBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: EsteticaViewHolder, position: Int) {
        val service = differ.currentList[position]
        holder.bind(service)
    }
}