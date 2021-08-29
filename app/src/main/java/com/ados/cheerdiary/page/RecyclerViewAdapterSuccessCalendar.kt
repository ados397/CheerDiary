package com.ados.cheerdiary.page

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.FurangCalendar
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.ListItemFanClubBinding
import com.ados.cheerdiary.databinding.ListItemSuccessCalendarBinding
import kotlinx.android.synthetic.main.list_item_success_calendar.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//class RecyclerViewAdapterSuccessCalendar(val context: Context, val calendarLayout: LinearLayout, val date: Date) : RecyclerView.Adapter<RecyclerViewAdapterSuccessCalendar.CalendarItemHolder>() {
class RecyclerViewAdapterSuccessCalendar(private val calendarLayout: LinearLayout, val date: Date) : RecyclerView.Adapter<RecyclerViewAdapterSuccessCalendar.CalendarItemHolder>() {
    private val TAG = javaClass.simpleName
    var dataList: ArrayList<Int> = arrayListOf()

    // FurangCalendar을 이용하여 날짜 리스트 세팅
    var furangCalendar: FurangCalendar = FurangCalendar(date)
    init {
        furangCalendar.initBaseCalendar()
        dataList = furangCalendar.dateList

        //println("캘린더 갯수 : $dataList")
    }

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null

    override fun onBindViewHolder(holder: CalendarItemHolder, position: Int) {

        //println("뷰홀더 호출")
        // list_item_calendar 높이 지정
        val h = calendarLayout.height / 6
        holder.itemView.layoutParams.height = h

        holder?.bind(dataList[position], position)
        if (itemClick != null) {
            holder?.itemView?.setOnClickListener { v ->
                itemClick?.onClick(v, position)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarItemHolder {
        //println("뷰홀더 생성")
        //val view = LayoutInflater.from(context).inflate(R.layout.list_item_success_calendar, parent, false)
        val view = ListItemSuccessCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarItemHolder(view)
    }

    override fun getItemCount(): Int {
        //println("사이즈 ${dataList.size}")
        return dataList.size
    }

    //inner class CalendarItemHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
    inner class CalendarItemHolder(private val viewBinding: ListItemSuccessCalendarBinding) : RecyclerView.ViewHolder(viewBinding.root) {

        //var itemCalendarDateText: TextView = itemView!!.item_calendar_date_text
        var itemCalendarDateText = viewBinding.itemCalendarDateText
        var process = viewBinding.progressPercent

        fun bind(data: Int, position: Int) {
            val firstDateIndex = furangCalendar.prevTail
            val lastDateIndex = dataList.size - furangCalendar.nextHead - 1

            // 날짜 표시
            itemCalendarDateText.text = data.toString()

            // 오늘 날짜 처리
            var dateString: String = SimpleDateFormat("dd", Locale.KOREA).format(date)
            var dateInt = dateString.toInt()
            if (dataList[position] == dateInt) {
                itemCalendarDateText.setTypeface(itemCalendarDateText.typeface, Typeface.BOLD)
            }

            // 현재 월의 1일 이전, 현재 월의 마지막일 이후 값의 텍스트를 회색처리
            if (position < firstDateIndex || position > lastDateIndex) {
                //itemCalendarDateText.setTextAppearance(R.style.LightColorTextViewStyle)
                //itemCalendarDotView.background = null
                process.visibility = View.GONE
            }
        }

    }
}
