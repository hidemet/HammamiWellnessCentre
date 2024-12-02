package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.domain.model.Service
import com.example.hammami.databinding.ItemBeneficiBinding


class ServiceDetailAdapter : ListAdapter<Service, ServiceDetailAdapter.ViewHolder>(ServiceDetailDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBeneficiBinding.inflate(
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
        //private val binding: ServizioDettaglioBinding
        private val binding: ItemBeneficiBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(service: Service) {
                with(binding) {
                    setupBeneficiInfo(service)
                    //setupClickListener(onServiceClicked)
                }
            }

        private fun ItemBeneficiBinding.setupBeneficiInfo(service: Service) {
            val beneficiList = service.benefits?.split("*")

            beneficiList?.forEach { beneficio ->
                //tvBenefici.append("$beneficio\n")  // Aggiungiamo \n per andare a capo
                tvBenefici.text = beneficio
            }
        }

                 /*
                 tvTitolo.text = service.name
                 tvContenutoDescrizione.text = service.description
                 tvContenutoDurata.text = "${service.length} min"
                 tvContenutoPrezzo.text = "${service.price} €"

                 /*
                 if (service.benefits != null) {
                    tvContenutoBenefici.text = service.benefits
                } else {
                    tvTitoloBenefici.visibility = View.GONE
                    tvContenutoBenefici.visibility = View.GONE
                }
                  */

                 if (service.reviews == null){
                     tvContenutoRecensioni.visibility = View.VISIBLE
                     rvRecensioni.visibility = View.GONE
                 }

                  */

            }

            /*
            private fun ItemRecensioneBinding.setupReviewInfo(review: Review) {
                tvNameUser.text = review.utente
                tvReviewUser.text = review.commento
                ratingBar.rating = review.valutazione.toFloat()
            }


            private fun ServizioDettaglioBinding.setupClickListener(service: Service) {
                root.setOnClickListener {
                    //onServiceSelected(service)
                }
            }

             */

    private class ServiceDetailDiffCallback : DiffUtil.ItemCallback<Service>() {
        override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem == newItem
    }
}
    /*

    inner class ServizioViewHolder(private val binding: ServizioDettaglioBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service) {
            binding.apply {
                tvTitolo.text = service.name
                tvContenutoDescrizione.text = service.description
                tvContenutoDurata.text = "${service.length} h"
                tvContenutoPrezzo.text = "${service.price} €"
                root.setOnClickListener { onItemClick(service) }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServizioViewHolder {
        return ServizioViewHolder(
            ServizioDettaglioBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ServizioViewHolder, position: Int) {
        val service = differ.currentList[position]
        holder.bind(service)
    }
}

     */