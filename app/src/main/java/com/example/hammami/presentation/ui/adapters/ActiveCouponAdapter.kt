package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.databinding.ItemActiveCouponBinding
import com.example.hammami.domain.model.coupon.Coupon
import com.example.hammami.domain.model.coupon.getFormattedExpirationDate

class ActiveCouponAdapter(
    private val onCopyCode: (String) -> Unit
) : ListAdapter<Coupon, ActiveCouponAdapter.ViewHolder>(CouponDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveCouponBinding.inflate(
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
        private val binding: ItemActiveCouponBinding,
        private val onCopyCode: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(coupon: Coupon) = with(binding) {
            setupCouponInfo(coupon)
            setupClickListener(coupon)
        }

        private fun ItemActiveCouponBinding.setupCouponInfo(coupon: Coupon) {
            couponValue.text = root.context.getString(
                R.string.coupon_value_format,
                coupon.value
            )
            couponCode.setText(coupon.code)
            couponExpiry.text = root.context.getString(
                R.string.coupon_expiration_format,
                coupon.getFormattedExpirationDate()

            )
        }

        private fun ItemActiveCouponBinding.setupClickListener(coupon: Coupon) = with(binding) {

            // Rendi l'intero layout cliccabile per una migliore UX (Legge di Fitts)
            root.setOnClickListener {
                onCopyCode(coupon.code)
            }

            // Aggiungi anche il click sull'endIcon come punto di interazione aggiuntivo
            couponCodeLayout.setEndIconOnClickListener {
                onCopyCode(coupon.code)
            }
        }
    }


    private class CouponDiffCallback : DiffUtil.ItemCallback<Coupon>() {
        override fun areItemsTheSame(oldItem: Coupon, newItem: Coupon): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Coupon, newItem: Coupon): Boolean =
            oldItem == newItem
    }
}
