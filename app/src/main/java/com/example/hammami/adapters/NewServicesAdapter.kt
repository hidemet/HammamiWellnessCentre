package com.example.hammami.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hammami.data.Service
import com.example.hammami.databinding.ItemHomepageBinding
import com.google.firebase.storage.FirebaseStorage

private val TAG = "NewServicesAdapter"
class NewServicesAdapter : RecyclerView.Adapter<NewServicesAdapter.NewServicesViewHolder>() {

    inner class NewServicesViewHolder(private val binding: ItemHomepageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(service: Service) {
            Log.d(TAG, "Binding service: $service")
            binding.apply {
                service.image?.let { imageRef ->
                    FirebaseStorage.getInstance().reference.child(imageRef.path).downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(itemView).load(uri).into(imageNewRvItem)
                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "Error loading image: ${exception.message}")
                    }
                }
                tvNewRvItemName.text = service.name
                tvNewItemRvPrice.text = "${service.price} €"
                // If you want to display the duration, you can add it here
                // For example: tvNewItemRvDuration.text = "Duration: ${service.length}"
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewServicesViewHolder {
        return NewServicesViewHolder(
            ItemHomepageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: NewServicesViewHolder, position: Int) {
        val service = differ.currentList[position]
        holder.bind(service)
    }
}