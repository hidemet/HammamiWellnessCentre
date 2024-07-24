package com.example.hammami.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hammami.data.Service
import com.example.hammami.databinding.ItemHomepageBinding

class RecommendedAdapter: RecyclerView.Adapter<RecommendedAdapter.RecommendedViewHolder>() {

    inner class RecommendedViewHolder(private val binding: ItemHomepageBinding):
        RecyclerView.ViewHolder(binding.root){

        fun bind(service: Service){
            binding.apply {
                Glide.with(itemView).load(service.image).into(imageNewRvItem)
                tvNewRvItemName.text = service.name
                tvNewItemRvPrice.text = service.price.toString()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendedViewHolder {
        return RecommendedViewHolder(
            ItemHomepageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RecommendedViewHolder, position: Int) {
        val service = differ.currentList[position]
        holder.bind(service)
    }

}