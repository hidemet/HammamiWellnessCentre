package com.example.hammami.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hammami.data.Service
import com.example.hammami.databinding.ItemHomepageBinding

class NewServicesAdapter: RecyclerView.Adapter<NewServicesAdapter.NewServicesViewHolder>() {

    inner class NewServicesViewHolder(private val binding: ItemHomepageBinding):
        RecyclerView.ViewHolder(binding.root){

            fun bind(service: Service){
                binding.apply {
                    Glide.with(itemView).load(service.image).into(imageNewRvItem)
                    tvNewRvItemName.text = service.name
                    tvNewItemRvPrice.text = service.price
                }
            }
        }

    private val diffCallback = object : DiffUtil.ItemCallback<Service>(){

        override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewServicesViewHolder {
        return NewServicesViewHolder(
            ItemHomepageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: NewServicesViewHolder, position: Int) {
        val service = differ.currentList[position]
        holder.bind(service)
    }


}