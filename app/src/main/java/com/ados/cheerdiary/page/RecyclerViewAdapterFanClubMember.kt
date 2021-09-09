package com.ados.cheerdiary.page

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.databinding.ListItemFanClubMemberBinding
import com.ados.cheerdiary.model.MemberDTO

class RecyclerViewAdapterFanClubMember(private val items: ArrayList<MemberDTO>, var clickListener: OnFanClubMemberItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterFanClubMember.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemFanClubMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(items[position], clickListener)

        items[position].let { item ->
            with(holder) {
                name.text = "${item.userNickname}"
                contribution.text = "기여도 : ${item.contribution}"

                imagePosition.setImageResource(item.getPositionImage())
                positionText.text = item.getPositionString()

                imageCheckout.setImageResource(item.getCheckoutImage())

                if (item.isSelected) {
                    mainLayout.setBackgroundColor(Color.parseColor("#BBD5F8"))
                } else {
                    mainLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }
        }
    }

    // 이미 선택된 항목을 선택할 경우 선택을 해제하고 false 반환, 아닐경우 해당항목 선택 후 true 반환
    fun selectItem(position: Int) : Boolean {
        return if (items[position].isSelected) {
            items[position].isSelected = false
            notifyDataSetChanged()
            false
        } else {
            for (item in items) {
                item.isSelected = false
            }
            items[position].isSelected = true
            notifyDataSetChanged()
            true
        }
    }

    inner class ViewHolder(private val viewBinding: ListItemFanClubMemberBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var imagePosition = viewBinding.imgPosition
        var name = viewBinding.textName
        var contribution = viewBinding.textContribution
        var positionText = viewBinding.textPosition
        var mainLayout = viewBinding.layoutMain
        var imageCheckout = viewBinding.imgCheckout

        fun initializes(item: MemberDTO, action:OnFanClubMemberItemClickListener) {
            viewBinding.layoutMain.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }
    }


}

interface OnFanClubMemberItemClickListener {
    fun onItemClick(item: MemberDTO, position: Int)
}