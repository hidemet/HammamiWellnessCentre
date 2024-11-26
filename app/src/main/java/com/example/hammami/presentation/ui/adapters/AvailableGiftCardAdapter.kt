package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.databinding.ItemAvailableVoucherBinding
import com.example.hammami.domain.model.coupon.AvailableVoucher
import com.example.hammami.domain.model.giftCard.AvailableGiftCard

class AvailableGiftCardAdapter(
private val onGiftCardSelected: (NavController, AvailableVoucher) -> Unit
) : ListAdapter<AvailableGiftCard, AvailableGiftCardAdapter.ViewHolder>(GiftCardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAvailableVoucherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onGiftCardSelected)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemAvailableVoucherBinding,
        private val onGiftCardSelected: (NavController, AvailableGiftCard) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(giftCard: AvailableGiftCard) {
            with(binding) {

                itemIcon.setImageResource(R.drawable.ic_gift_card)

                voucherValue.text = root.context.getString(
                    R.string.gift_card_value_format,
                    giftCard.value
                )
                root.setOnClickListener {
                    if (giftCard.isEnabled) {
                        onGiftCardSelected(root.findNavController(),giftCard)
                    }
                }
            }
        }
    }

    private class GiftCardDiffCallback : DiffUtil.ItemCallback<AvailableGiftCard>() {
        override fun areItemsTheSame(oldItem: AvailableGiftCard, newItem: AvailableGiftCard): Boolean =
            oldItem.value == newItem.value

        override fun areContentsTheSame(oldItem: AvailableGiftCard, newItem: AvailableGiftCard): Boolean =
            oldItem == newItem
    }
}