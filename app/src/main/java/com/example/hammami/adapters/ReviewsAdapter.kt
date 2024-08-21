package com.example.hammami.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hammami.R
import com.example.hammami.data.Review
import com.example.hammami.data.Service
import com.example.hammami.databinding.ItemHomepageBinding
import com.example.hammami.databinding.ItemRecensioneBinding
import com.example.hammami.databinding.ServizioDettaglioBinding

class ReviewsAdapter : RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder>() {

    inner class ReviewsViewHolder(private val binding: ItemRecensioneBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review) {
            binding.apply {
                tvNameUser.text = review.utente
                tvReviewUser.text = review.commento
                ratingBar.rating = review.valutazione.toFloat()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        return ReviewsViewHolder(
            ItemRecensioneBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        val service = differ.currentList[position]
        holder.bind(service)

        holder.itemView.setOnClickListener {
            onClick?.invoke(service)
        }
    }

    var onClick: ((Service) -> Unit)? = null
}