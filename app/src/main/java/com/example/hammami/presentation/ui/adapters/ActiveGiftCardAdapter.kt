package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.databinding.ItemActiveGiftCardBinding
import com.example.hammami.domain.model.giftCard.GiftCard
import java.text.SimpleDateFormat
import java.util.Locale

class ActiveGiftCardAdapter(
    private val onCopyCode: (String) -> Unit
) : ListAdapter<GiftCard, ActiveGiftCardAdapter.ViewHolder>(GiftCardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.example.hammami.databinding.ItemActiveGiftCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onCopyCode)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemActiveGiftCardBinding,
        private val onCopyCode: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(giftCard: GiftCard) {
            with(binding) {
                giftCardValue.text = root.context.getString(
                    R.string.gift_card_value_format,
                    giftCard.value
                )
                giftCardCode.setText(giftCard.code)
                setupExpiryDate(giftCard)
                setupCopyButton(giftCard)
            }
        }


        private fun ItemActiveGiftCardBinding.setupExpiryDate(giftCard: GiftCard) {
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(giftCard.expirationDate.toDate())
            giftCardExpiry.text = root.context.getString(
                R.string.expires_on,
                formattedDate
            )
        }

        private fun ItemActiveGiftCardBinding.setupCopyButton(giftCard: GiftCard) {
            giftCardCodeLayout.setEndIconOnClickListener {
                onCopyCode(giftCard.code)
            }
        }
    }

    private class GiftCardDiffCallback : DiffUtil.ItemCallback<GiftCard>() {
        override fun areItemsTheSame(oldItem: GiftCard, newItem: GiftCard): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: GiftCard, newItem: GiftCard): Boolean =
            oldItem == newItem
    }
}