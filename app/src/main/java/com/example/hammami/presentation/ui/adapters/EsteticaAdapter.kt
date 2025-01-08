package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.domain.model.Service
import com.example.hammami.databinding.ItemEsteticaBinding
import com.example.hammami.databinding.ItemMassaggiBenessereBinding
import com.example.hammami.presentation.ui.features.client.HomeFragmentDirections
import com.example.hammami.presentation.ui.features.service.BenessereFragmentDirections
import com.example.hammami.presentation.ui.features.service.EsteticaFragmentDirections


class EsteticaAdapter : ListAdapter<Service, EsteticaAdapter.ViewHolder>(EsteticaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEsteticaBinding.inflate(
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
        private val binding: ItemEsteticaBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(service: Service) {
            with(binding) {
                setupServiceInfo(service)
                setupClickListener(service)
            }
        }

        private fun ItemEsteticaBinding.setupServiceInfo(service: Service) {
            tvTitle.text = service.name
            tvPrice.text = "${service.price} €"
        }

        private fun ItemEsteticaBinding.setupClickListener(service: Service) {
            root.setOnClickListener {
                //onServiceSelected(service)
                //Navigation.findNavController(root).navigate(R.id.action_benessereFragment_to_servizioDetailFragment)
                //ServizioDettaglioBinding.setupServiceDetails(service)
                val action =
                    EsteticaFragmentDirections.actionEsteticaFragmentToServiceDetailFragment(
                        service
                    )
                it.findNavController().navigate(action)
            }
        }
    }

    }

    private class EsteticaDiffCallback : DiffUtil.ItemCallback<Service>() {
        override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem == newItem
    }


    /*
    inner class EsteticaViewHolder(private val binding: ItemEsteticaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service) {
            binding.apply {
                tvTitle.text = service.name
                tvPrice.text = "${service.price} €"
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EsteticaViewHolder {
        return EsteticaViewHolder(
            ItemEsteticaBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: EsteticaViewHolder, position: Int) {
        val service = differ.currentList[position]
        holder.bind(service)
    }

     */