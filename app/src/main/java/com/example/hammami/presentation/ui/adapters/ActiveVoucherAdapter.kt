package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.databinding.ItemActiveVoucherBinding
import com.example.hammami.domain.model.DiscountVoucher
import com.example.hammami.domain.model.VoucherType
import java.text.SimpleDateFormat
import java.util.Locale

class ActiveVoucherAdapter(
    private val onCopyCode: (String) -> Unit,
) : ListAdapter<DiscountVoucher, ActiveVoucherAdapter.ViewHolder>(VoucherDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveVoucherBinding.inflate(
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
        private val binding: ItemActiveVoucherBinding,
        private val onCopyCode: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(voucher: DiscountVoucher) = with(binding) {
            // Imposta il titolo in base al tipo
            titleText.text = when(voucher.type) {
                VoucherType.GIFT_CARD -> root.context.getString(R.string.gift_card)
                VoucherType.COUPON -> root.context.getString(R.string.coupon)
            }

            // Imposta il valore
            voucherValue.text = root.context.getString(
                R.string.voucher_value_format,
                voucher.value
            )

            // Imposta il codice
            voucherCode.setText(voucher.code)

            // Imposta la data di scadenza
            voucherExpiry.text = root.context.getString(
                R.string.expires_on,
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(voucher.expirationDate.toDate())
            )

            // Setup del click per la copia
            voucherCodeLayout.setEndIconOnClickListener {
                onCopyCode(voucher.code)
            }
        }
    }

    private class VoucherDiffCallback : DiffUtil.ItemCallback<DiscountVoucher>() {
        override fun areItemsTheSame(
            oldItem: DiscountVoucher,
            newItem: DiscountVoucher
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: DiscountVoucher,
            newItem: DiscountVoucher
        ): Boolean = oldItem == newItem
    }
}