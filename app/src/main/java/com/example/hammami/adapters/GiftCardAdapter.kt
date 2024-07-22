package com.example.hammami.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.databinding.ItemGiftCardBinding

class GiftCardAdapter(
    private var giftCardValues: List<Int>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<GiftCardAdapter.GiftCardViewHolder>() {

    inner class GiftCardViewHolder(private val binding: ItemGiftCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(value: Int) {
            binding.giftCardValue.text = "$value €"
            binding.giftCardTitle.text = "Buono regalo"
            binding.giftCardDescription.text = "Riscatta per tutti i servizi"

            binding.root.setOnClickListener {
                onItemClick(value)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftCardViewHolder {
        val binding =
            ItemGiftCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GiftCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GiftCardViewHolder, position: Int) {
        holder.bind(giftCardValues[position])
    }

    override fun getItemCount() = giftCardValues.size

    fun updateGiftCards(newGiftCardValues: List<Int>) {
        giftCardValues = newGiftCardValues
        notifyDataSetChanged()
    }
}