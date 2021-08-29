package com.ados.cheerdiary.page

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.databinding.ListItemScheduleBinding
import com.ados.cheerdiary.model.ScheduleDTO

class RecyclerViewAdapterSchedule(private val items: ArrayList<ScheduleDTO>, var clickListener: OnScheduleItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterSchedule.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(items[position], clickListener)

        items[position].let { item ->
            with(holder) {
                title.text = "${item.title}"

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

    inner class ViewHolder(private val viewBinding: ListItemScheduleBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var title = viewBinding.textTitle
        var mainLayout = viewBinding.layoutMain

        fun initializes(item: ScheduleDTO, action:OnScheduleItemClickListener) {
            viewBinding.layoutItem.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }
    }


}

interface OnScheduleItemClickListener {
    fun onItemClick(item: ScheduleDTO, position: Int)
}