package com.example.hammami.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.databinding.ItemCouponBinding
class CouponAdapter(
    private var couponValues: List<Int>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<CouponAdapter.CouponViewHolder>() {

    inner class CouponViewHolder(private val binding: ItemCouponBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(value: Int) {
            binding.couponValue.text = "$value €"
            binding.couponPoints.text = "Richiede ${value * 5} pt"

            binding.root.setOnClickListener {
                onItemClick(value)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val binding =
            ItemCouponBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CouponViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        holder.bind(couponValues[position])
    }

    override fun getItemCount() = couponValues.size

    fun updateCoupons(newCouponValues: List<Int>) {
        couponValues = newCouponValues
        notifyDataSetChanged()
    }
}