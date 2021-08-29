package com.ados.cheerdiary.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentSuccessCalendarBinding
import com.ados.cheerdiary.model.FanClubDTO
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentSuccessCalendar.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentSuccessCalendar : Fragment(), OnFanClubItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentSuccessCalendarBinding? = null
    private val binding get() = _binding!!

    //lateinit var mContext: Context

    var pageIndex = Int.MAX_VALUE
    lateinit var currentDate: Date

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterSuccessCalendar

    //lateinit var calendar_year_month_text: TextView
    //lateinit var calendar_layout: LinearLayout
    //lateinit var calendar_view: RecyclerView
    lateinit var calendarAdapter: RecyclerViewAdapterSuccessCalendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSuccessCalendarBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerView = rootView.findViewById(R.id.rv_success_calendar!!)as RecyclerView
        //recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = GridLayoutManager(activity, 7)

        initView(rootView)

        var calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, 2021)
        calendar.set(Calendar.MONTH, 8)


        for (i in 1..31) {
            calendar.set(Calendar.DATE, i)
            println("현재 날짜 ${i}")
            println("WEEK_OF_MONTH ? = ${calendar.get(Calendar.WEEK_OF_MONTH)}")
            println("DAY_OF_MONTH ? = ${calendar.get(Calendar.DAY_OF_MONTH)}")
            println("MONTH ? = ${calendar.get(Calendar.MONTH)}")
        }
        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    fun initView(view: View) {
        pageIndex -= (Int.MAX_VALUE / 2)
        //Log.e(TAG, "Calender Index: $pageIndex")
        //calendar_year_month_text = view.calendar_year_month_text
        //calendar_layout = view.calendar_layout
        //calendar_view = view.calendar_view
        // 날짜 적용
        println("날짜 표시 $pageIndex")

        val date = Calendar.getInstance().run {
            add(Calendar.MONTH, pageIndex)
            time
        }
        //binding.rvSuccessCalendar.adapter = RecyclerViewAdapterSuccessCalendar(binding.layoutSuccessCalendar, date)
        //binding.rvSuccessCalendar.adapter = RecyclerViewAdapterSuccessCalendar(requireContext(), binding.layoutSuccessCalendar, date)
        var datas : ArrayList<FanClubDTO> = arrayListOf()
        datas.apply {
            for (i in 0..100) {
                add(FanClubDTO(false, "영웅시대", "", 10, 0.0, "홍길동", 10 ))
                add(FanClubDTO(false, "희랑별", "", 5, 0.0, "홍길동", 7 ))
                add(FanClubDTO(false, "우주총동원", "", 7, 0.0, "홍길동", 9 ))
            }
        }

        recyclerViewAdapter = RecyclerViewAdapterSuccessCalendar(binding.layoutSuccessCalendar, date)
        recyclerView.adapter = recyclerViewAdapter

        currentDate = date
        //Log.e(TAG, "$date")
        // 포맷 적용
        var datetime: String = SimpleDateFormat("yyyy년 MM월").format(date.time)
        binding.calendarYearMonthText.setText(datetime)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentSuccessCalendar.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentSuccessCalendar().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: FanClubDTO, position: Int) {
        TODO("Not yet implemented")
    }
}