package com.ados.cheerdiary.page

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.SuccessCalendarWeek
import com.ados.cheerdiary.databinding.ListItemMissionBinding
import com.ados.cheerdiary.model.DashboardMissionDTO
import com.ados.cheerdiary.model.ScheduleDTO
import com.ados.cheerdiary.model.ScheduleProgressDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdapterMission(private val items: ArrayList<DashboardMissionDTO>, var clickListener: OnMissionItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterMission.ViewHolder>() {

    private var firebaseAuth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemMissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(items[position], clickListener)

        items[position].let { item ->
            with(holder) {
                title.text = "${item.scheduleDTO?.title}"
                count.text = "${item.scheduleProgressDTO?.count}/${item.scheduleDTO?.count}"

                var percentage = ((item.scheduleProgressDTO?.count?.toDouble()!! / item.scheduleDTO?.count!!) * 100).toInt()
                percent.text = "$percentage%"
                progress.progress = percentage
            }
        }
    }

    inner class ViewHolder(private val viewBinding: ListItemMissionBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var title = viewBinding.textTitle
        var count = viewBinding.textCount
        var percent = viewBinding.textPercent
        var progress = viewBinding.progressPercent
        var mainLayout = viewBinding.layoutMain

        fun initializes(item: DashboardMissionDTO, action:OnMissionItemClickListener) {
            viewBinding.layoutMain.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }
    }


}

interface OnMissionItemClickListener {
    fun onItemClick(item: DashboardMissionDTO, position: Int)
}