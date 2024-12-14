package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.databinding.ItemAvailableVoucherBinding
import com.example.hammami.domain.model.AvailableVoucher

class AvailableGiftCardAdapter(
    private val onGiftCardSelected: (NavController, AvailableVoucher) -> Unit
) : ListAdapter<AvailableVoucher, AvailableGiftCardAdapter.ViewHolder>(GiftCardDiffCallback()) {

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
        private val onGiftCardSelected: (NavController, AvailableVoucher) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(giftCard: AvailableVoucher) = with(binding) {
            itemIcon.setImageResource(R.drawable.ic_gift_card)

            titleText.text = root.context.getString(R.string.gift_card)
            voucherValue.text = root.context.getString(
                R.string.gift_card_value_format,
                giftCard.value
            )

            // Nasconde i punti per le gift card
            voucherPoints.isVisible = false

            root.setOnClickListener {
                onGiftCardSelected(root.findNavController(), giftCard)
            }
        }
    }

    private class GiftCardDiffCallback : DiffUtil.ItemCallback<AvailableVoucher>() {
        override fun areItemsTheSame(oldItem: AvailableVoucher, newItem: AvailableVoucher): Boolean =
            oldItem.value == newItem.value

        override fun areContentsTheSame(oldItem: AvailableVoucher, newItem: AvailableVoucher): Boolean =
            oldItem == newItem
    }
}