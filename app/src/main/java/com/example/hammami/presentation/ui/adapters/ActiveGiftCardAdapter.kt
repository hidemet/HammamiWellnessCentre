package com.example.hammami.presentation.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.databinding.ItemActiveVoucherBinding
import com.example.hammami.domain.model.Voucher
import com.example.hammami.domain.model.VoucherType
import java.text.SimpleDateFormat
import java.util.Locale

class ActiveGiftCardAdapter(
    private val onCopyCode: (String) -> Unit
) : ListAdapter<Voucher, ActiveGiftCardAdapter.ViewHolder>(VoucherDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveVoucherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onCopyCode)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val voucher = getItem(position)
        Log.d("ActiveGiftCardAdapter", "Binding voucher at position $position: $voucher")
        holder.bind(voucher)
    }

    class ViewHolder(
        private val binding: ItemActiveVoucherBinding,
        private val onCopyCode: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(voucher: Voucher) = with(binding) {
            titleText.text = root.context.getString(R.string.gift_card)
            icon.setImageResource(R.drawable.ic_gift_card)

            voucherValue.text = root.context.getString(
                R.string.gift_card_value_format,
                voucher.value
            )

            voucherCodeLayout.apply {
                voucherCode.setText(voucher.code)
                hint = root.context.getString(R.string.gift_card_code)
                setEndIconOnClickListener { onCopyCode(voucher.code) }
            }

            voucherExpiry.text = root.context.getString(
                R.string.expires_on,
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(voucher.expirationDate.toDate())
            )
        }
    }

    class VoucherDiffCallback : DiffUtil.ItemCallback<Voucher>() {
        override fun areItemsTheSame(oldItem: Voucher, newItem: Voucher): Boolean =
            oldItem.code == newItem.code
        override fun areContentsTheSame(oldItem: Voucher, newItem: Voucher): Boolean =
            oldItem == newItem
    }
}