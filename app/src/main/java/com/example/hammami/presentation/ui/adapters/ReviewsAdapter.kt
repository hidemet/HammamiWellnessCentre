package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.domain.model.Review
import com.example.hammami.databinding.ItemRecensioneBinding

class ReviewsAdapter : ListAdapter<Review, ReviewsAdapter.ViewHolder>(ReviewsDiffCallback())  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecensioneBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemRecensioneBinding,
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(review: Review) {
                with(binding) {
                    setupReviewInfo(review)
                }
            }

            private fun ItemRecensioneBinding.setupReviewInfo(review: Review) {
                tvNameUser.text = review.utente
                tvReviewUser.text = review.commento
                ratingBar.rating = review.valutazione.toFloat()
            }

        }

    private class ReviewsDiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean =
            oldItem == newItem
    }
}

    /*

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

    private val diffCallback = object : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
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
        val review = differ.currentList[position]
        holder.bind(review)

        holder.itemView.setOnClickListener {
            onClick?.invoke(review)
        }
    }

    var onClick: ((Review) -> Unit)? = null

     */