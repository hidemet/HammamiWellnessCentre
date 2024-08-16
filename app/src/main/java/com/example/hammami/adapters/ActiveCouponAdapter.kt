package com.example.hammami.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.databinding.ItemActiveCouponBinding
import com.example.hammami.models.Coupon
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ActiveCouponAdapter(
    private val onItemClick: (Coupon) -> Unit
) : ListAdapter<Coupon, ActiveCouponAdapter.ViewHolder>(CouponDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveCouponBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateCoupons(newCoupons: List<Coupon>) {
        submitList(newCoupons)
    }

    inner class ViewHolder(private val binding: ItemActiveCouponBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(coupon: Coupon) {
            binding.couponCode.text = coupon.code
            binding.couponValue.text = "${coupon.value} €"
            binding.couponExpiration.text = "Scade il: ${formatDate(coupon.expirationDate)}"
            binding.root.setOnClickListener { onItemClick(coupon) }
        }
    }

    private fun formatDate(timestamp: Timestamp): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(timestamp.toDate())
    }

    class CouponDiffCallback : DiffUtil.ItemCallback<Coupon>() {
        override fun areItemsTheSame(oldItem: Coupon, newItem: Coupon): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: Coupon, newItem: Coupon): Boolean {
            return oldItem == newItem
        }
    }
}