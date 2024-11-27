package com.example.hammami.presentation.ui.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.databinding.ItemAvailableVoucherBinding
import com.example.hammami.domain.model.AvailableVoucher

class AvailableCouponAdapter(
    private val onCouponSelected: (AvailableVoucher) -> Unit
) : ListAdapter<AvailableVoucher, AvailableCouponAdapter.ViewHolder>(CouponDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAvailableVoucherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onCouponSelected)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemAvailableVoucherBinding,
        private val onCouponSelected: (AvailableVoucher) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(coupon: AvailableVoucher) {
            with(binding) {
                root.isEnabled = coupon.isEnabled
                // Applica l'alpha quando disabilitato
                root.alpha = if (coupon.isEnabled) 1.0f else 0.38f

                itemIcon.setImageResource(R.drawable.ic_coupon)

                voucherValue.text = itemView.context.getString(
                    R.string.coupon_value_format,
                    coupon.value
                )
                voucherPoints.text = itemView.context.getString(
                    R.string.points_required_format,
                    coupon.requiredPoints
                )

                root.setOnClickListener {
                    if (coupon.isEnabled) {
                        onCouponSelected(coupon)
                    }
                }
            }
        }
    }

    private class CouponDiffCallback : DiffUtil.ItemCallback<AvailableVoucher>() {
        override fun areItemsTheSame(oldItem: AvailableVoucher, newItem: AvailableVoucher): Boolean =
            oldItem.value == newItem.value &&
                    oldItem.requiredPoints == newItem.requiredPoints

        override fun areContentsTheSame(oldItem: AvailableVoucher, newItem: AvailableVoucher): Boolean =
            oldItem == newItem
    }
}