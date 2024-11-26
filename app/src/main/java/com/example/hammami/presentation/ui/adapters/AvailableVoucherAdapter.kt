package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.databinding.ItemAvailableVoucherBinding
import com.example.hammami.domain.AvailableVoucher
import com.example.hammami.domain.model.VoucherType

class AvailableVoucherAdapter(
    private val onVoucherSelected: (AvailableVoucher) -> Unit
) : ListAdapter<AvailableVoucher, AvailableVoucherAdapter.ViewHolder>(AvailableVoucherDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAvailableVoucherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onVoucherSelected)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemAvailableVoucherBinding,
        private val onVoucherSelected: (AvailableVoucher) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(voucher: AvailableVoucher) = with(binding) {
            root.isEnabled = voucher.isEnabled
            root.alpha = if (voucher.isEnabled) 1.0f else 0.38f

            // Imposta l'icona in base al tipo
            itemIcon.setImageResource(
                when(voucher.type) {
                    VoucherType.GIFT_CARD -> R.drawable.ic_gift_card
                    VoucherType.COUPON -> R.drawable.ic_coupon
                }
            )

            // Imposta il titolo
            voucherValue.text = when(voucher.type) {
                VoucherType.GIFT_CARD -> itemView.context.getString(
                    R.string.gift_card_value_format,
                    voucher.value
                )
                VoucherType.COUPON -> itemView.context.getString(
                    R.string.coupon_value_format,
                    voucher.value
                )
            }

            // Mostra i punti richiesti solo per i coupon
            voucherPoints.isVisible = voucher.type == VoucherType.COUPON
            if (voucher.type == VoucherType.COUPON && voucher.requiredPoints != null) {
                voucherPoints.text = itemView.context.getString(
                    R.string.points_required_format,
                    voucher.requiredPoints
                )
            }

            root.setOnClickListener {
                if (voucher.isEnabled) {
                    onVoucherSelected(voucher)
                }
            }
        }
    }

    private class AvailableVoucherDiffCallback : DiffUtil.ItemCallback<AvailableVoucher>() {
        override fun areItemsTheSame(
            oldItem: AvailableVoucher,
            newItem: AvailableVoucher
        ): Boolean = oldItem.value == newItem.value &&
                oldItem.type == newItem.type

        override fun areContentsTheSame(
            oldItem: AvailableVoucher,
            newItem: AvailableVoucher
        ): Boolean = oldItem == newItem
    }
}