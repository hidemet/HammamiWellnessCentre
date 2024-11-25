package com.example.hammami.presentation.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.hammami.R
import com.example.hammami.domain.model.InfoHammami

class InfoAdapter (private var mList: List<InfoHammami>) :

    RecyclerView.Adapter<InfoAdapter.InfoViewHolder>() {
        inner class InfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.logoIv)
            val titleTv: TextView = itemView.findViewById(R.id.titleTv)
            val langDescTv: TextView = itemView.findViewById(R.id.itemDesc)
            val constraintLayout: ConstraintLayout = itemView.findViewById(R.id.constraintLayoutTitle)

            fun collapseExpandedView(){
                langDescTv.visibility = View.GONE
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setFilteredList(mList: List<InfoHammami>) {
            this.mList = mList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.info_item, parent, false)
            return InfoViewHolder(view)
        }

        override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {

            val infoData = mList[position]
            holder.icon.setImageResource(infoData.icon)
            holder.titleTv.text = infoData.title
            holder.langDescTv.text = infoData.desc

            val isExpandable: Boolean = infoData.isExpandable
            holder.langDescTv.visibility = if (isExpandable) View.VISIBLE else View.GONE

            holder.constraintLayout.setOnClickListener {
                isAnyItemExpanded(position)
                infoData.isExpandable = !infoData.isExpandable
                notifyItemChanged(position , Unit)
            }

        }

        private fun isAnyItemExpanded(position: Int){
            val temp = mList.indexOfFirst {
                it.isExpandable
            }
            if (temp >= 0 && temp != position){
                mList[temp].isExpandable = false
                notifyItemChanged(temp , 0)
            }
        }

        override fun onBindViewHolder(
            holder: InfoViewHolder,
            position: Int,
            payloads: MutableList<Any>
        ) {

            if(payloads.isNotEmpty() && payloads[0] == 0){
                holder.collapseExpandedView()
            }else{
                super.onBindViewHolder(holder, position, payloads)

            }
        }

        override fun getItemCount(): Int {
            return mList.size
        }

    }
