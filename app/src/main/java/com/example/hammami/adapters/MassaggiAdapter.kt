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

class MassaggiAdapter : RecyclerView.Adapter<MassaggiAdapter.MassaggiViewHolder>() {

    inner class MassaggiViewHolder(private val binding: ItemMassaggiBenessereBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service) {
            binding.apply {
                tvTitle.text = service.name
                tvPrice.text = "${service.price} €"
                tvDescription.text = service.description
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MassaggiViewHolder {
        return MassaggiViewHolder(
            ItemMassaggiBenessereBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: MassaggiViewHolder, position: Int) {
        val service = differ.currentList[position]
        holder.bind(service)
    }
}