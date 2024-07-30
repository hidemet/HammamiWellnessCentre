package com.example.hammami.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hammami.R
import com.example.hammami.data.Service
import com.example.hammami.databinding.ItemHomepageBinding
import com.google.firebase.storage.FirebaseStorage

private val TAG = "RecommendedAdapter"

class RecommendedAdapter : RecyclerView.Adapter<RecommendedAdapter.RecommendedViewHolder>() {

    inner class RecommendedViewHolder(private val binding: ItemHomepageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service) {
            binding.apply {
                if (service.image != null) {
                    FirebaseStorage.getInstance().reference.child("gs:/hammami-9adc7.appspot.com/Immagini/massaggio4mani.jpg").downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(itemView).load("gs:/hammami-9adc7.appspot.com/Immagini/massaggio4mani.jpg).into(imageNewRvItem").into(imageNewRvItem)
                        println(service.image!!.path)
                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "Impossibile ottenere l'URL di download: ${exception.message}", exception)
                        // Carica un'immagine placeholder o nascondi l'ImageView
                        println(service.image!!.path)
                        imageNewRvItem.setImageResource(R.drawable.ic_appuntamenti)
                    }
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