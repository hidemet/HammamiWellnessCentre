package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.domain.model.Service
import com.example.hammami.databinding.ItemMassaggiBenessereBinding
import com.example.hammami.presentation.ui.adapters.BenessereAdapter.*
import com.example.hammami.presentation.ui.features.service.BenessereFragmentDirections


class BenessereAdapter(
    //private val onServiceSelected: (Service) -> Unit
) : ListAdapter<Service, ViewHolder>(BenessereDiffCallback()) {

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
        return ViewHolder(
            binding, //onServiceSelected
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemMassaggiBenessereBinding,
        //private val onServiceClicked: (Service) -> Unit
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
        private fun ServizioDettaglioBinding.setupServiceDetails(service: Service) {

            // DA FARE: IMMAGINE/PLACEHOLDER

            tvTitolo.text = service.name
            tvContenutoDescrizione.text = service.description
            tvContenutoDurata.text = "${service.length} min"
            tvContenutoPrezzo.text = "${service.price} €"

            if (service.benefits != null) {
                tvContenutoBenefici.text = service.benefits
            } else {
                tvTitoloBenefici.visibility = View.GONE
                tvContenutoBenefici.visibility = View.GONE
                //tvContenutoBenefici.text = "Nessun beneficio specificato"
            }

            // DA FARE: RECENSIONI

            /*
            Glide.with(root)
                .load(service.imageUrl)
                .centerCrop()
                .into(ivService)
             */
        }

         */

        private fun ItemMassaggiBenessereBinding.setupClickListener(service: Service) {
            root.setOnClickListener {
                //onServiceSelected(service)
                //Navigation.findNavController(root).navigate(R.id.action_benessereFragment_to_servizioDetailFragment)
                //ServizioDettaglioBinding.setupServiceDetails(service)
                val action =
                    BenessereFragmentDirections.actionBenessereFragmentToServiceDetailFragment(
                        service
                    )
                it.findNavController().navigate(action)
            }

        }
    }

    private class BenessereDiffCallback : DiffUtil.ItemCallback<Service>() {
        override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem == newItem
    }

}