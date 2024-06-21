package com.example.hammami.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.data.Service
import com.example.hammami.databinding.NewRvItemBinding

class NewServicesAdapter: RecyclerView.Adapter<NewServicesAdapter.NewServicesViewHolder>() {

    inner class NewServicesViewHolder(private val binding: NewRvItemBinding): RecyclerView.ViewHolder(binding.root)

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
            NewRvItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: NewServicesViewHolder, position: Int) {
        TODO("Not yet implemented")
    }


}