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
import com.example.hammami.databinding.MembroTeamBinding
import com.example.hammami.models.CentreMember

class CentreMemberAdapter /* : RecyclerView.Adapter<CentreMemberAdapter.CentreMemberViewHolder>() */ {
/*
    inner class CentreMemberViewHolder(private val binding: MembroTeamBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(centreMember: CentreMember) {
            Glide.with(itemView).load(centreMember.image).into(ivCard)
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<CentreMember>() {
        override fun areItemsTheSame(oldItem: CentreMember, newItem: CentreMember): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: CentreMember, newItem: CentreMember): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CentreMemberViewHolder {
        return CentreMemberViewHolder(
            MembroTeamBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CentreMemberViewHolder, position: Int) {
        val service = differ.currentList[position]
        holder.bind(service)
    }
    */
}