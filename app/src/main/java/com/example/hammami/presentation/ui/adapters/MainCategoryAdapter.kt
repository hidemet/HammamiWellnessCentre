package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hammami.R
import com.example.hammami.domain.model.Service
import com.example.hammami.databinding.ItemHomepageBinding
import com.example.hammami.presentation.ui.features.service.MainCategoryFragmentDirections
import com.example.hammami.presentation.ui.features.client.HomeFragmentDirections

class MainCategoryAdapter() : ListAdapter<Service, MainCategoryAdapter.ViewHolder>(MainCategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomepageBinding.inflate(
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
        private val binding: ItemHomepageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(service: Service) {
            with(binding) {
                setupServiceInfo(service)
                setupClickListener(service)
            }
        }

        private fun ItemHomepageBinding.setupServiceInfo(service: Service) {
            if (service.image != null) {
                Glide.with(itemView).load(service.image).into(imageNewRvItem)
            } else {
                // Carica un'immagine placeholder o nascondi l'ImageView
                imageNewRvItem.setImageResource(R.drawable.placeholder_image)
            }
            //Glide.with(itemView).load(service.image).into(imageNewRvItem)
            tvNewRvItemName.text = service.name
            tvNewItemRvPrice.text = "${service.price} €"
        }

        private fun ItemHomepageBinding.setupClickListener(service: Service) {
            root.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToServiceDetailFragment(service)
                it.findNavController().navigate(action)
            }
        }
    }

}

    private class MainCategoryDiffCallback : DiffUtil.ItemCallback<Service>() {
        override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem == newItem
    }

/*

    inner class BestDealsViewHolder(private val binding: ItemHomepageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service) {
            binding.apply {
                if (service.image != null) {
                    Glide.with(itemView).load(service.image).into(imageNewRvItem)
                } else {
                    // Carica un'immagine placeholder o nascondi l'ImageView
                    imageNewRvItem.setImageResource(R.drawable.placeholder_image)
                }
                tvNewRvItemName.text = service.name
                tvNewItemRvPrice.text = "${service.price} €"
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestDealsViewHolder {
        return BestDealsViewHolder(
            ItemHomepageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BestDealsViewHolder, position: Int) {
        val service = differ.currentList[position]
        holder.bind(service)

        holder.itemView.setOnClickListener {
            onClick?.invoke(service)
        }
    }

    var onClick: ((Service) -> Unit)? = null
}


 */