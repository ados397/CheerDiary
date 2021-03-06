package com.ados.cheerdiary.page

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.size
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentScheduleAddBinding
import com.ados.cheerdiary.dialog.SelectAppDialog
import com.ados.cheerdiary.model.FanClubDTO
import com.ados.cheerdiary.model.MemberDTO
import com.ados.cheerdiary.model.ScheduleDTO
import com.applikeysolutions.cosmocalendar.selection.OnDaySelectedListener
import com.applikeysolutions.cosmocalendar.selection.RangeSelectionManager
import com.applikeysolutions.cosmocalendar.utils.SelectionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    private var _binding: FragmentScheduleAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    private var firebaseAuth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    var scheduleDTO = ScheduleDTO()
    private var titleOK: Boolean = false
    private var rangeOK: Boolean = false
    private var purposeOK: Boolean = false
    private var actionOK: Boolean = false
    private var cycleOK: Boolean = false
    private var countOK: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            fanClubDTO = it.getParcelable(ARG_PARAM1)
            currentMember = it.getParcelable(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentScheduleAddBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.layoutSelectApp.visibility = View.GONE
        binding.editUrl.visibility = View.GONE
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

        if (!scheduleDTO.docName.isNullOrEmpty()) {
            titleOK = true
            rangeOK = true
            purposeOK = true
            actionOK = true
            cycleOK = true
            countOK = true

            binding.editTitle.setText(scheduleDTO.title)
            binding.textTitleLen.text = "${binding.editTitle.text.length}/30"
            setStartEndDate()
            binding.editPurpose.setText(scheduleDTO.purpose)
            binding.textPurposeLen.text = "${binding.editPurpose.text.length}/300"
            when (scheduleDTO.action) {
                ScheduleDTO.ACTION.APP -> {
                    binding.textSelectedApp.text = scheduleDTO.appDTO?.appName
                    binding.radioApp.isChecked = true
                    selectActionApp()
                }
                ScheduleDTO.ACTION.URL -> {
                    binding.editUrl.setText(scheduleDTO.url)
                    binding.radioUrl.isChecked = true
                    selectActionUrl()
                }
            }
            when (scheduleDTO.cycle) {
                ScheduleDTO.CYCLE.DAY -> binding.radioDay.isChecked = true
                ScheduleDTO.CYCLE.WEEK -> binding.radioWeek.isChecked = true
                ScheduleDTO.CYCLE.MONTH -> binding.radioMonth.isChecked = true
                ScheduleDTO.CYCLE.PERIOD -> binding.radioPeriod.isChecked = true
            }
            binding.editCount.setText(scheduleDTO.count.toString())

            if (scheduleDTO.isAlarm == true) {
                binding.layoutAlarm.visibility = View.VISIBLE
                binding.timepickerAlarm.currentHour = scheduleDTO.alarmDTO.alarmHour!!
                binding.timepickerAlarm.currentMinute = scheduleDTO.alarmDTO.alarmMinute!!

                if (scheduleDTO.alarmDTO.alarmDate != null) {
                    setCalendarSingleSubExecute()
                } else {
                    binding.daySun.isChecked = scheduleDTO.alarmDTO.dayOfWeek["1"] == true
                    binding.dayMon.isChecked = scheduleDTO.alarmDTO.dayOfWeek["2"] == true
                    binding.dayTues.isChecked = scheduleDTO.alarmDTO.dayOfWeek["3"] == true
                    binding.dayWed.isChecked = scheduleDTO.alarmDTO.dayOfWeek["4"] == true
                    binding.dayThurs.isChecked = scheduleDTO.alarmDTO.dayOfWeek["5"] == true
                    binding.dayFri.isChecked = scheduleDTO.alarmDTO.dayOfWeek["6"] == true
                    binding.daySat.isChecked = scheduleDTO.alarmDTO.dayOfWeek["7"] == true
                    setWeekString()
                }
                binding.switchAlarm.isChecked = true
            }

            visibleOkButton()
        } else {
            getAlarmDateText(binding.timepickerAlarm.currentHour, binding.timepickerAlarm.currentMinute)
        }

        binding.editPurpose.setOnTouchListener { view, motionEvent ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.buttonSelectApp.setOnClickListener {
            val dialog = SelectAppDialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            dialog.button_app_cancel.setOnClickListener { // No
                dialog.dismiss()
            }

            dialog.button_app_ok.setOnClickListener { // Ok
                if (dialog.selectedApp == null) {
                    Toast.makeText(activity,"????????? ?????? ????????????.", Toast.LENGTH_SHORT).show()
                } else {
                    scheduleDTO.appDTO = dialog.selectedApp
                    binding.textSelectedApp.text = scheduleDTO.appDTO?.appName
                    dialog.dismiss()
                    actionOK = true
                    visibleOkButton()
                }
            }
        }

        binding.editStartDate.setOnClickListener {
            setCalendarRange()
        }
        binding.editEndDate.setOnClickListener {
            setCalendarRange()
        }

        binding.radioGroupAction.setOnCheckedChangeListener { radioGroup, i ->
            when(i) {
                R.id.radio_app -> {
                    selectActionApp()
                }
                R.id.radio_url -> {
                    selectActionUrl()
                }
            }
        }

        binding.switchAlarm.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                binding.layoutAlarm.visibility = View.VISIBLE
                scheduleDTO.isAlarm = true
            } else {
                binding.layoutAlarm.visibility = View.GONE
                scheduleDTO.isAlarm = false
            }
        }

        binding.buttonAlarmCalendar.setOnClickListener {
            setCalendarSingle()
        }

        binding.timepickerAlarm.setOnTimeChangedListener { timePicker, i, i2 ->
            if (binding.weekGroup.checkedIds.size == 0) {
                getAlarmDateText(i, i2)
            }
        }

        binding.weekGroup.setOnCheckedChangeListener { group, checkedId, isChecked ->
            setWeekString()
        }

        binding.editTitle.doAfterTextChanged {
            if (binding.editTitle.text.toString().isNullOrEmpty()) {
                binding.textTitleError.text = "????????? ????????? ?????????."
                binding.editTitle.setBackgroundResource(R.drawable.edit_rectangle_red)
                titleOK = false
            } else {
                binding.textTitleError.text = ""
                binding.editTitle.setBackgroundResource(R.drawable.edit_rectangle)
                titleOK = true
            }

            binding.textTitleLen.text = "${binding.editTitle.text.length}/30"

            visibleOkButton()
        }

        binding.editPurpose.doAfterTextChanged {
            if (binding.editPurpose.text.toString().isNullOrEmpty()) {
                binding.textPurposeError.text = "????????? ????????? ?????????."
                binding.editPurpose.setBackgroundResource(R.drawable.edit_rectangle_red)
                purposeOK = false
            } else {
                binding.textPurposeError.text = ""
                binding.editPurpose.setBackgroundResource(R.drawable.edit_rectangle)
                purposeOK = true
            }

            binding.textPurposeLen.text = "${binding.editPurpose.text.length}/300"

            visibleOkButton()
        }

        binding.editUrl.doAfterTextChanged {
            isValidUrlEdit()
        }

        binding.editCount.doAfterTextChanged {
            var countStr = binding.editCount.text.toString()
            var count = 0
            if (!countStr.isNullOrEmpty()) {
                count = countStr.toInt()
            }

            if (countStr.isNullOrEmpty() || count <= 0) {
                binding.textCountError.text = "?????? ????????? 1 ?????? ????????? ?????????."
                binding.editCount.setBackgroundResource(R.drawable.edit_rectangle_red)
                countOK = false
            } else {
                binding.textCountError.text = ""
                binding.editCount.setBackgroundResource(R.drawable.edit_rectangle)
                countOK = true
            }
            visibleOkButton()
        }

        binding.radioGroupCycle.setOnCheckedChangeListener { radioGroup, i ->
            when(i) {
                R.id.radio_day -> {
                    scheduleDTO.cycle = ScheduleDTO.CYCLE.DAY
                    cycleOK = true
                }
                R.id.radio_week -> {
                    scheduleDTO.cycle = ScheduleDTO.CYCLE.WEEK
                    cycleOK = true
                }
                R.id.radio_month -> {
                    scheduleDTO.cycle = ScheduleDTO.CYCLE.MONTH
                    cycleOK = true
                }
                R.id.radio_period -> {
                    scheduleDTO.cycle = ScheduleDTO.CYCLE.PERIOD
                    cycleOK = true
                }
            }
            visibleOkButton()
        }

        binding.buttonOk.setOnClickListener {
            if (scheduleDTO.docName.isNullOrEmpty()) {
                val alphabets = ('a'..'z').toMutableList()
                scheduleDTO.docName = "${alphabets[Random().nextInt(alphabets.size)]}${System.currentTimeMillis()}"
                scheduleDTO.order = System.currentTimeMillis()
            }

            scheduleDTO.isSelected = false
            scheduleDTO.title = binding.editTitle.text.toString()
            scheduleDTO.purpose = binding.editPurpose.text.toString()
            scheduleDTO.url = binding.editUrl.text.toString()

            // ????????? ????????? ?????? ???????????? ????????????
            when (scheduleDTO.action) {
                ScheduleDTO.ACTION.APP -> {
                    scheduleDTO.url = ""
                }
                ScheduleDTO.ACTION.URL -> {
                    scheduleDTO.appDTO = null
                }
            }

            scheduleDTO.count = binding.editCount.text.toString().toLong()
            scheduleDTO.alarmDTO.alarmHour = binding.timepickerAlarm.currentHour
            scheduleDTO.alarmDTO.alarmMinute = binding.timepickerAlarm.currentMinute

            //firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.document()?.set(scheduleDTO)?.addOnCompleteListener {
            if (fanClubDTO == null) {
                setPersonalSchedule()
            } else {
                setFanClubSchedule()
            }
        }


        /*binding.buttonExecuteApp.setOnClickListener {
            val linePackage = "com.iloen.melon"

            val intentLine = requireActivity().packageManager.getLaunchIntentForPackage(linePackage) // ???????????? ????????? ?????? ??????

            try {
                startActivity(intentLine) // ?????? ?????? ???????????????.
            } catch (e: Exception) {  // ?????? ????????? ???????????? (?????? ?????????)
                val intentPlayStore = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + linePackage)) // ?????? ????????? ???????????? ??????
                startActivity(intentPlayStore) // ????????????????????? ??????
            }

        }*/
    }

    private fun setPersonalSchedule() {
        firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.document(scheduleDTO.docName.toString())?.set(scheduleDTO)?.addOnCompleteListener {
            Toast.makeText(activity,"????????? ?????? ??????", Toast.LENGTH_SHORT).show()
            finishFragment()
        }
    }

    private fun setFanClubSchedule() {
        firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("schedule")?.document(scheduleDTO.docName.toString())?.set(scheduleDTO)?.addOnCompleteListener {
            Toast.makeText(activity,"????????? ????????? ?????? ??????", Toast.LENGTH_SHORT).show()
            finishFragment()
        }
    }

    private fun selectActionApp() {
        binding.layoutSelectApp.visibility = View.VISIBLE
        binding.editUrl.visibility = View.GONE
        binding.textUrlError.visibility = View.GONE

        scheduleDTO.action = ScheduleDTO.ACTION.APP
        actionOK = scheduleDTO.appDTO != null
        visibleOkButton()
    }

    private fun selectActionUrl() {
        binding.layoutSelectApp.visibility = View.GONE
        binding.editUrl.visibility = View.VISIBLE
        binding.textUrlError.visibility = View.VISIBLE

        scheduleDTO.action = ScheduleDTO.ACTION.URL
        isValidUrlEdit()
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
        if (fanClubDTO == null) {
            finishFragment()
        } else {
            finishFragment()
        }
    }

    private fun finishFragment() {
        val fragment = FragmentScheduleList.newInstance(fanClubDTO, currentMember)
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun finishFragmentFanClub() {
        val fragment = FragmentFanClubSchedule()
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
            // ?????? ???????????? ?????? ??????
            var today = SimpleDateFormat("MM??? dd??? (E)", Locale("ko", "KR")).format(Date())
            binding.textAlarmDate.text = "??????-$today"
        } else {
            // ?????? ???????????? ????????? ????????? ??????
            val cal = Calendar.getInstance()
            cal.time = Date()
            cal.add(Calendar.DATE, 1)
            var tomorrow = SimpleDateFormat("MM??? dd??? (E)", Locale("ko", "KR")).format(cal.time)

            // ????????? ????????? ????????? ?????? ??????
            val alarmYear = SimpleDateFormat("yyyy").format(cal.time).toInt()
            val todayYear = SimpleDateFormat("yyyy").format(Date()).toInt()
            if (alarmYear > todayYear) { // ????????? ????????? ?????? ??????
                tomorrow = "${alarmYear}??? $tomorrow"
            }

            binding.textAlarmDate.text = "??????-$tomorrow"
        }
    }

    private fun setWeekString() {
        scheduleDTO.alarmDTO.alarmDate = null
        scheduleDTO.alarmDTO.clearDayOfWeek()
        when {
            binding.weekGroup.checkedIds.size == 0 -> {
                getAlarmDateText(binding.timepickerAlarm.currentHour, binding.timepickerAlarm.currentMinute)
            }
            binding.weekGroup.checkedIds.size >= binding.weekGroup.size -> {
                binding.textAlarmDate.text = "??????"
                scheduleDTO.alarmDTO.everyDayOfWeek()
            }
            else -> {
                var weekSize = 0
                var weekString = ""

                if (binding.daySun.isChecked) {
                    weekString += "???"
                    weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["1"] = true
                }
                if (binding.dayMon.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "???"
                    weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["2"] = true
                }
                if (binding.dayTues.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "???"
                    weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["3"] = true
                }
                if (binding.dayWed.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "???"
                    weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["4"] = true
                }
                if (binding.dayThurs.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "???"
                    weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["5"] = true
                }
                if (binding.dayFri.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "???"
                    weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["6"] = true
                }
                if (binding.daySat.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "???"
                    weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["7"] = true
                }
                binding.textAlarmDate.text = "?????? $weekString"
            }
        }
        println("?????? ?????? ${scheduleDTO.alarmDTO.dayOfWeek}")
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
                scheduleDTO.startDate = binding.calendarView.selectedDays[0].calendar.time
                scheduleDTO.endDate = binding.calendarView.selectedDays[binding.calendarView.selectedDays.lastIndex].calendar.time

                setStartEndDate()
            }
            isValidCalendarRange()
        }

        binding.buttonCalCancel.setOnClickListener {
            hideCalendar()
            isValidCalendarRange()
        }
    }

    private fun setStartEndDate() {
        var startDate = SimpleDateFormat("yyyy.MM.dd").format(scheduleDTO.startDate)
        var endDate = SimpleDateFormat("yyyy.MM.dd").format(scheduleDTO.endDate)

        println("????????? ${scheduleDTO.startDate}, ?????? ${scheduleDTO.endDate}")

        binding.editStartDate.setText(startDate)
        binding.editEndDate.setText(endDate)
    }

    private fun isValidCalendarRange() {
        if (binding.editStartDate.text.isNullOrEmpty() || binding.editEndDate.text.isNullOrEmpty()) {
            binding.textRangeError.text = "????????? ????????? ?????????."
            binding.editStartDate.setBackgroundResource(R.drawable.edit_rectangle_red)
            binding.editEndDate.setBackgroundResource(R.drawable.edit_rectangle_red)
            rangeOK = false
        } else {
            binding.textRangeError.text = ""
            binding.editStartDate.setBackgroundResource(R.drawable.edit_rectangle)
            binding.editEndDate.setBackgroundResource(R.drawable.edit_rectangle)
            rangeOK = true
        }
        visibleOkButton()
    }

    private fun isValidUrl(url: String) : Boolean {
        return url.matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]".toRegex())
    }

    private fun isValidUrlEdit() {
        //if (binding.editUrl.text.toString().isNullOrEmpty()) {
        if (!isValidUrl(binding.editUrl.text.toString())) {
            binding.textUrlError.text = "????????? URL??? ????????????."
            binding.editUrl.setBackgroundResource(R.drawable.edit_rectangle_red)
            actionOK = false
        } else {
            binding.textUrlError.text = ""
            binding.editUrl.setBackgroundResource(R.drawable.edit_rectangle)
            actionOK = true
        }

        visibleOkButton()
    }

    private fun setCalendarSingleSubExecute() {
        val alarmDate = SimpleDateFormat("yyyyMMdd").format(scheduleDTO.alarmDTO.alarmDate).toInt()
        val todayDate = SimpleDateFormat("yyyyMMdd").format(Date()).toInt()

        var alarmDateFormat = SimpleDateFormat("MM??? dd??? (E)", Locale("ko", "KR")).format(scheduleDTO.alarmDTO.alarmDate)
        val alarmYear = SimpleDateFormat("yyyy").format(scheduleDTO.alarmDTO.alarmDate).toInt()
        val todayYear = SimpleDateFormat("yyyy").format(Date()).toInt()
        if (alarmYear > todayYear) { // ????????? ????????? ?????? ??????
            alarmDateFormat = "${alarmYear}??? $alarmDateFormat"
        }

        when (alarmDate) {
            todayDate -> { // ??????
                val alarmTime = "${binding.timepickerAlarm.currentHour}${binding.timepickerAlarm.currentMinute}".toInt()
                val nowTime = SimpleDateFormat("HHmm").format(Date()).toInt()

                //println("${binding.timepickerAlarm.hour}")
                if (alarmTime > nowTime) {
                    binding.textAlarmDate.text = "??????-$alarmDateFormat"
                } else { // ?????? ?????? ????????? ?????? ??????
                    Toast.makeText(activity,"?????? ?????? ????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show()
                }
            }
            (todayDate + 1) -> { // ??????
                binding.textAlarmDate.text = "??????-$alarmDateFormat"
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
                scheduleDTO.alarmDTO.alarmDate = binding.calendarView.selectedDays[0].calendar.time
                if (binding.weekGroup.checkedIds.size > 0) {
                    binding.weekGroup.clearCheck()
                    Handler().postDelayed({ // weekGroup ?????? ??????????????? ????????? ????????? ????????? ??????
                        setCalendarSingleSubExecute()
                    }, 50L)
                } else {
                    setCalendarSingleSubExecute()
                }


                //binding.weekGroup.isEnabled = true

            }
        }

        binding.buttonCalCancel.setOnClickListener {
            hideCalendar()
        }
    }

    // ????????? ??????
    private fun visibleOkButton() {
        binding.buttonOk.isEnabled = titleOK && rangeOK && purposeOK && actionOK && cycleOK && countOK

        println("????????? $titleOK && $rangeOK && $purposeOK && $actionOK && $cycleOK && $countOK")
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
        fun newInstance(param1: FanClubDTO?, param2: MemberDTO?) =
            FragmentScheduleAdd().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }
}