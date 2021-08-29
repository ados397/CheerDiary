package com.ados.cheerdiary.page

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.databinding.ListItemFanClubSignUpBinding
import com.ados.cheerdiary.model.SignUpDTO

class RecyclerViewAdapterFanClubSignUp(private val items: ArrayList<SignUpDTO>, var clickListener: OnFanClubSignUpItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterFanClubSignUp.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemFanClubSignUpBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(items[position], clickListener)

        items[position].let { item ->
            with(holder) {
                checkBox.isChecked = item.isChecked
                name.text = "${item.name}"


                if (item.isSelected) {
                    mainLayout.setBackgroundColor(Color.parseColor("#BBD5F8"))
                } else {
                    mainLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }
        }
    }

    // 체크된 항목이 하나라도 있으면 true 반환 함수
    fun isChecked() : Boolean {
        for (item in items) {
            if (item.isChecked) {
                return true
            }
        }
        return false
    }

    inner class ViewHolder(private val viewBinding: ListItemFanClubSignUpBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var checkBox = viewBinding.checkbox
        var name = viewBinding.textName
        var mainLayout = viewBinding.layoutMain

        fun initializes(item: SignUpDTO, action:OnFanClubSignUpItemClickListener) {
            viewBinding.checkbox.setOnClickListener {
                item.isChecked = viewBinding.checkbox.isChecked
                action.onItemClick(item, adapterPosition)
            }
        }
    }


}

interface OnFanClubSignUpItemClickListener {
    fun onItemClick(item: SignUpDTO, position: Int)
}