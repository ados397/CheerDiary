package com.ados.cheerdiary.page

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentScheduleAddBinding
import com.ados.cheerdiary.dialog.SelectAppDialog
import com.applikeysolutions.cosmocalendar.selection.OnDaySelectedListener
import com.applikeysolutions.cosmocalendar.selection.RangeSelectionManager
import com.applikeysolutions.cosmocalendar.utils.SelectionType
import kotlinx.android.synthetic.main.select_app_dialog.*
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentScheduleAdd.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentScheduleAdd : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentScheduleAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

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
        _binding = FragmentScheduleAddBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        binding.layoutSelectApp.visibility = View.GONE
        binding.editLink.visibility = View.GONE
        binding.layoutAlarm.visibility = View.GONE

        val c = Calendar.getInstance()

        val disabledDaysSet: MutableSet<Long> = HashSet()
        for (day in c.get(Calendar.DAY_OF_MONTH) downTo 1){
            c.add(Calendar.DATE, -1)
            disabledDaysSet.add(c.timeInMillis)
        }
        binding.calendarView.disabledDays = disabledDaysSet

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAlarmDateText(binding.timepickerAlarm.currentHour, binding.timepickerAlarm.currentMinute)

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.buttonSelectApp.setOnClickListener {
            val dialog = SelectAppDialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            dialog.button_cancel.setOnClickListener { // No
                dialog.dismiss()
            }

            dialog.button_ok.setOnClickListener { // Ok
                binding.textSelectedApp.text = dialog.selectedAppName

                dialog.dismiss()
            }
        }

        binding.editStartDate.setOnClickListener {
            setCalendarRange()
        }
        binding.editEndDate.setOnClickListener {
            setCalendarRange()
        }

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when(i) {
                R.id.radio_app -> {
                    binding.layoutSelectApp.visibility = View.VISIBLE
                    binding.editLink.visibility = View.GONE
                }
                R.id.radio_link -> {
                    binding.layoutSelectApp.visibility = View.GONE
                    binding.editLink.visibility = View.VISIBLE
                }
            }
        }

        binding.switchAlarm.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                binding.layoutAlarm.visibility = View.VISIBLE
            } else {
                binding.layoutAlarm.visibility = View.GONE
            }
        }

        binding.buttonAlarmCalendar.setOnClickListener {
            setCalendarSingle()
        }

        binding.buttonCalCancel.setOnClickListener {
            hideCalendar()
        }

        binding.timepickerAlarm.setOnTimeChangedListener { timePicker, i, i2 ->
            if (binding.weekGroup.checkedIds.size == 0) {
                getAlarmDateText(i, i2)
            }
        }

        binding.weekGroup.setOnCheckedChangeListener { group, checkedId, isChecked ->
            setWeekString()
        }



        /*binding.buttonExecuteApp.setOnClickListener {
            val linePackage = "com.iloen.melon"

            val intentLine = requireActivity().packageManager.getLaunchIntentForPackage(linePackage) // 인텐트에 패키지 주소 저장

            try {
                startActivity(intentLine) // 라인 앱을 실행해본다.
            } catch (e: Exception) {  // 만약 실행이 안된다면 (앱이 없다면)
                val intentPlayStore = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + linePackage)) // 설치 링크를 인텐트에 담아
                startActivity(intentPlayStore) // 플레이스토어로 이동
            }

        }*/
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun callBackPressed() {
        val fragment = FragmentScheduleList()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun getAlarmDateText(hour: Int, min: Int) {
        val alarmTime = "${hour}${min}".toInt()
        val nowTime = SimpleDateFormat("HHmm").format(Date()).toInt()
        if (alarmTime > nowTime) {
            // 현재 시간보다 크면 오늘
            var today = SimpleDateFormat("MM월 dd일 (E)", Locale("ko", "KR")).format(Date())
            binding.textAlarmDate.text = "오늘-$today"
        } else {
            // 현재 시간보다 작거나 같으면 내일
            val cal = Calendar.getInstance()
            cal.time = Date()
            cal.add(Calendar.DATE, 1)
            var tomorrow = SimpleDateFormat("MM월 dd일 (E)", Locale("ko", "KR")).format(cal.time)

            // 내일이 년도가 바뀌면 년도 표시
            val alarmYear = SimpleDateFormat("yyyy").format(cal.time).toInt()
            val todayYear = SimpleDateFormat("yyyy").format(Date()).toInt()
            if (alarmYear > todayYear) { // 년도가 바뀌면 년도 표시
                tomorrow = "${alarmYear}년 $tomorrow"
            }

            binding.textAlarmDate.text = "내일-$tomorrow"
        }
    }

    private fun setWeekString() {
        when {
            binding.weekGroup.checkedIds.size == 0 -> {
                getAlarmDateText(binding.timepickerAlarm.currentHour, binding.timepickerAlarm.currentMinute)
            }
            binding.weekGroup.checkedIds.size >= binding.weekGroup.size -> {
                binding.textAlarmDate.text = "매일"
            }
            else -> {
                var weekSize = 0
                var weekString = ""

                if (binding.daySun.isChecked) {
                    weekString += "일"
                    weekSize++
                }
                if (binding.dayMon.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "월"
                    weekSize++
                }
                if (binding.dayTues.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "화"
                    weekSize++
                }
                if (binding.dayWed.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "수"
                    weekSize++
                }
                if (binding.dayThurs.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "목"
                    weekSize++
                }
                if (binding.dayFri.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "금"
                    weekSize++
                }
                if (binding.daySat.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "토"
                    weekSize++
                }
                binding.textAlarmDate.text = "매주 $weekString"
            }
        }



    }

    private fun showCalendar() {
        binding.scrollView.visibility = View.GONE
        binding.layoutOk.visibility = View.GONE
        binding.layoutCalendar.visibility = View.VISIBLE
    }

    private fun hideCalendar() {
        binding.scrollView.visibility = View.VISIBLE
        binding.layoutOk.visibility = View.VISIBLE
        binding.layoutCalendar.visibility = View.GONE
    }

    private fun setCalendarRange() {
        showCalendar()
        binding.calendarView.selectedDayBackgroundColor = Color.parseColor("#FFEACA")
        binding.calendarView.selectionType = SelectionType.RANGE
        binding.calendarView.selectionManager = RangeSelectionManager(OnDaySelectedListener {
            /*println("========== setSelectionManager ==========")
            println("Selected Dates : " + binding.calendarView.selectedDates.size)
            if (binding.calendarView.selectedDates.size <= 0) return@OnDaySelectedListener
            println("Selected Days : " + binding.calendarView.selectedDays)
            println("Start Day : " + binding.calendarView.selectedDays[0])
            println("End Day : " + binding.calendarView.selectedDays[binding.calendarView.selectedDays.lastIndex])*/
        })

        binding.buttonCalOk.setOnClickListener {
            hideCalendar()
            if (binding.calendarView.selectedDates.size > 0) {
                var startDate = SimpleDateFormat("yyyy-MM-dd").format(binding.calendarView.selectedDays[0].calendar.time)
                var endDate = SimpleDateFormat("yyyy-MM-dd").format(binding.calendarView.selectedDays[binding.calendarView.selectedDays.lastIndex].calendar.time)

                binding.editStartDate.setText(startDate)
                binding.editEndDate.setText(endDate)
            }
        }
    }

    private fun setCalendarSingleSubExecute() {
        val alarmDate = SimpleDateFormat("yyyyMMdd").format(binding.calendarView.selectedDays[0].calendar.time).toInt()
        val todayDate = SimpleDateFormat("yyyyMMdd").format(Date()).toInt()

        var alarmDateFormat = SimpleDateFormat("MM월 dd일 (E)", Locale("ko", "KR")).format(binding.calendarView.selectedDays[0].calendar.time)
        val alarmYear = SimpleDateFormat("yyyy").format(binding.calendarView.selectedDays[0].calendar.time).toInt()
        val todayYear = SimpleDateFormat("yyyy").format(Date()).toInt()
        if (alarmYear > todayYear) { // 년도가 바뀌면 년도 표시
            alarmDateFormat = "${alarmYear}년 $alarmDateFormat"
        }

        when (alarmDate) {
            todayDate -> { // 오늘
                val alarmTime = "${binding.timepickerAlarm.currentHour}${binding.timepickerAlarm.currentMinute}".toInt()
                val nowTime = SimpleDateFormat("HHmm").format(Date()).toInt()

                //println("${binding.timepickerAlarm.hour}")
                if (alarmTime > nowTime) {
                    binding.textAlarmDate.text = "오늘-$alarmDateFormat"
                } else { // 이미 지난 시간은 설정 못함
                    Toast.makeText(activity,"이미 지난 시간은 선택할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            (todayDate + 1) -> { // 내일
                binding.textAlarmDate.text = "내일-$alarmDateFormat"
            }
            else -> {
                binding.textAlarmDate.text = "$alarmDateFormat"
            }
        }
    }

    private fun setCalendarSingle() {
        showCalendar()
        binding.calendarView.selectionType = SelectionType.SINGLE
        binding.calendarView.selectedDayBackgroundColor = Color.parseColor("#F79256")

        binding.buttonCalOk.setOnClickListener {
            hideCalendar()
            if (binding.calendarView.selectedDates.size > 0) {
                if (binding.weekGroup.checkedIds.size > 0) {
                    binding.weekGroup.clearCheck()
                    Handler().postDelayed({ // weekGroup 체크 해제하는데 시간이 걸려서 딜레이 추가
                        setCalendarSingleSubExecute()
                    }, 50L)
                } else {
                    setCalendarSingleSubExecute()
                }


                //binding.weekGroup.isEnabled = true

            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentScheduleAdd.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentScheduleAdd().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}