package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.domain.model.Service
import com.example.hammami.databinding.ItemMassaggiBenessereBinding
import com.example.hammami.presentation.ui.features.service.MassaggiFragmentDirections

class MassaggiAdapter : ListAdapter<Service, MassaggiAdapter.ViewHolder>(MassaggiDiffCallback()) {

    /*
    inner class BenessereViewHolder(private val binding: ItemMassaggiBenessereBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service) {
            binding.apply {
                tvTitle.text = service.name
                tvPrice.text = "${service.price} €"
                tvDescription.text = service.description
            }
        }
    }
    */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMassaggiBenessereBinding.inflate(
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
        private val binding: ItemMassaggiBenessereBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(service: Service) {
            with(binding) {
                setupServiceInfo(service)
                setupClickListener(service)
            }
        }

        private fun ItemMassaggiBenessereBinding.setupServiceInfo(service: Service) {
            tvTitle.text = service.name
            tvPrice.text = "${service.price} €"
            tvDescription.text = service.description
        }

        /*
        private fun ItemMassaggiBenessereBinding.setupClickListener(service: Service) {
            root.setOnClickListener {
                if (coupon.isEnabled) {
                    onCouponSelected(coupon)
                }
            }
        }
    }
    */

        private fun ItemMassaggiBenessereBinding.setupClickListener(service: Service) {
            root.setOnClickListener {
                val action =
                    MassaggiFragmentDirections.actionMassaggiFragmentToServiceDetailFragment(
                        service
                    )
                it.findNavController().navigate(action)
            }

        }

    }

    private class MassaggiDiffCallback : DiffUtil.ItemCallback<Service>() {
        override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem == newItem
    }

    /*
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

     */
}