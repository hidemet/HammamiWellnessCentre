package com.example.hammami.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.databinding.ItemActiveGiftCardBinding
import com.example.hammami.databinding.ItemActiveVoucherBinding
import com.example.hammami.domain.model.DiscountVoucher
import com.example.hammami.domain.model.VoucherType
import com.example.hammami.domain.model.giftCard.GiftCard
import java.text.SimpleDateFormat
import java.util.Locale

class ActiveGiftCardAdapter(
    private val onCopyCode: (String) -> Unit
) : ListAdapter<DiscountVoucher, ActiveGiftCardAdapter.ViewHolder>(VoucherDiffCallback()) {

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
        if (voucher.type == VoucherType.GIFT_CARD) {
            holder.bind(voucher)
        }
    }

    class ViewHolder(
        private val binding: ItemActiveVoucherBinding,
        private val onCopyCode: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(voucher: DiscountVoucher) = with(binding) {
            // Mostra titolo "Gift Card"
            titleText.text = root.context.getString(R.string.gift_card)

            // Mostra il valore della gift card
            voucherValue.text = root.context.getString(
                R.string.gift_card_value_format,
                voucher.value
            )

            // Setup codice gift card con copy
            voucherCodeLayout.apply {
                voucherCode.setText(voucher.code)
                hint = root.context.getString(R.string.gift_card_code)
                setEndIconOnClickListener {
                    onCopyCode(voucher.code)
                }
            }

            // Data di scadenza
            voucherExpiry.text = root.context.getString(
                R.string.expires_on,
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(voucher.expirationDate.toDate())
            )
        }
    }
}

// DiffCallback comune per entrambi gli adapter
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