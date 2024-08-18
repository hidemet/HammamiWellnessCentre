package com.example.hammami.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hammami.R
import com.example.hammami.data.Service
import com.example.hammami.databinding.ItemHomepageBinding
import com.example.hammami.databinding.ItemMassaggiBenessereBinding
import com.example.hammami.databinding.ServizioDettaglioBinding

class ServizioAdapter(private val onItemClick: (Service) -> Unit) : RecyclerView.Adapter<ServizioAdapter.ServizioViewHolder>() {

    inner class ServizioViewHolder(private val binding: ServizioDettaglioBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service) {
            binding.apply {
                tvTitolo.text = service.name
                tvContenutoDescrizione.text = service.description
                tvContenutoDurata.text = "${service.length} h"
                tvContenutoPrezzo.text = "${service.price} €"
                root.setOnClickListener { onItemClick(service) }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServizioViewHolder {
        return ServizioViewHolder(
            ServizioDettaglioBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ServizioViewHolder, position: Int) {
        val service = differ.currentList[position]
        holder.bind(service)
    }
}