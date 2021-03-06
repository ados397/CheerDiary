package com.ados.cheerdiary.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentDashboardMissionBinding
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.ados.cheerdiary.MainActivity
import com.ados.cheerdiary.R.drawable.btn_round
import com.ados.cheerdiary.SuccessCalendarWeek
import com.ados.cheerdiary.ToggleAnimation
import com.ados.cheerdiary.dialog.EditTextModifyDialog
import com.ados.cheerdiary.dialog.MissionDialog
import com.ados.cheerdiary.dialog.QuestionDialog
import com.ados.cheerdiary.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.edit_text_modify_dialog.*
import kotlinx.android.synthetic.main.mission_dialog.*
import kotlinx.android.synthetic.main.question_dialog.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentDashboardMission.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentDashboardMission : Fragment(), OnMissionItemClickListener {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentDashboardMissionBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    lateinit var recyclerViewFanClub : RecyclerView
    lateinit var recyclerViewFanClubAdapter : RecyclerViewAdapterMission
    lateinit var recyclerViewPersonal : RecyclerView
    lateinit var recyclerViewPersonalAdapter : RecyclerViewAdapterMission

    private var personalSchedules : ArrayList<ScheduleDTO> = arrayListOf()
    private var personalMissions : ArrayList<DashboardMissionDTO> = arrayListOf()
    private var fanClubSchedules : ArrayList<ScheduleDTO> = arrayListOf()
    private var fanClubMissions : ArrayList<DashboardMissionDTO> = arrayListOf()

    private var isExpandClub: Boolean = true
    private var isExpandPersonal: Boolean = true

    private var selectedCycle : ScheduleDTO.CYCLE = ScheduleDTO.CYCLE.DAY

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
        _binding = FragmentDashboardMissionBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerViewFanClub = rootView.findViewById(R.id.rv_mission_fan_club!!)as RecyclerView
        recyclerViewFanClub.layoutManager = LinearLayoutManager(requireContext())

        recyclerViewPersonal = rootView.findViewById(R.id.rv_mission_personal!!)as RecyclerView
        recyclerViewPersonal.layoutManager = LinearLayoutManager(requireContext())

        getFanClubSchedule()
        getPersonalSchedule()

        /*firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            personalSchedules.clear()
            personalMissions.clear()
            println("??????1 ${personalMissions.size}")
            for (document in result) {
                var schedule = document.toObject(ScheduleDTO::class.java)!!
                personalSchedules.add(schedule)

                var mission = DashboardMissionDTO()
                mission.scheduleDTO = schedule

                var docName = getProgressDocName(schedule)

                println("????????? : $schedule")
                println("????????? : $docName")
                firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.document(schedule.docName.toString())?.collection("progress")?.document(docName)?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("?????? ??????? $task")
                        var scheduleProgressDTO = ScheduleProgressDTO(docName, 0)
                        if (task.result.exists()) { // document ??????
                            scheduleProgressDTO = task.result.toObject(ScheduleProgressDTO::class.java)!!
                        }

                        mission.scheduleProgressDTO = scheduleProgressDTO

                        println("??????????????? : $scheduleProgressDTO")
                        personalMissions.add(mission)
                        println("??????2 ${personalMissions.size}")
                    }
                }
            }
            println("?????? ??????")

            timer(period = 100)
            {
                if (personalSchedules.size == personalMissions.size) {
                    cancel()
                    activity?.runOnUiThread {
                        setAdapter()
                        println("?????? ??????22")
                        println("??????3 ${personalMissions.size}")
                    }
                }
            }
        }?.addOnFailureListener { exception ->

        }*/




        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun toggleLayout(isExpanded: Boolean, view: View, fromRv: RecyclerView, toRv: RecyclerView): Boolean {
        ToggleAnimation.toggleButton(view, isExpanded)
        if (isExpanded) {
            ToggleAnimation.expand(fromRv, toRv)
        } else {
            ToggleAnimation.close(fromRv, toRv)
        }
        return isExpanded
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = (activity as MainActivity?)?.getUser()
        if (!user?.mainTitle.isNullOrEmpty()) {
            binding.textTitle.text = user?.mainTitle
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            (activity as MainActivity?)?.refreshFanClubDTO { fanClub ->
                fanClubDTO = fanClub

                (activity as MainActivity?)?.refreshMemberDTO { member ->
                    currentMember = member

                    getFanClubSchedule()
                    getPersonalSchedule()

                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(activity, "?????? ??????", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.textTitle.setOnClickListener {
            val user = (activity as MainActivity?)?.getUser()
            val item = EditTextDTO("?????? ??????", user?.mainTitle, 30)
            val dialog = EditTextModifyDialog(requireContext(), item)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_modify_cancel.setOnClickListener { // No
                dialog.dismiss()
            }
            dialog.button_modify_ok.setOnClickListener {
                dialog.dismiss()

                val question = QuestionDTO(
                    QuestionDTO.STAT.WARNING,
                    "?????? ??????",
                    "????????? ?????? ???????????????????",
                )
                val questionDialog = QuestionDialog(requireContext(), question)
                questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog.setCanceledOnTouchOutside(false)
                questionDialog.show()
                questionDialog.button_question_cancel.setOnClickListener { // No
                    questionDialog.dismiss()
                }
                questionDialog.button_question_ok.setOnClickListener {
                    questionDialog.dismiss()

                    user?.mainTitle = dialog.edit_content.text.toString()
                    firestore?.collection("user")?.document(user?.uid.toString())?.set(user!!)?.addOnCompleteListener {
                        Toast.makeText(activity, "?????? ?????? ??????!", Toast.LENGTH_SHORT).show()
                        binding.textTitle.text = dialog.edit_content.text

                        (activity as MainActivity?)?.setUser(user)
                    }
                }
            }
        }

        binding.buttonHideClub.setOnClickListener {
            if (isExpandClub) {
                isExpandClub = false
                binding.buttonHideClub.setImageResource(R.drawable.expend)
                toggleLayout(isExpandClub, binding.buttonHideClub, binding.rvMissionFanClub, binding.rvMissionPersonal)
            } else {
                isExpandClub = true
                binding.buttonHideClub.setImageResource(R.drawable.minimize)
                toggleLayout(isExpandClub, binding.buttonHideClub, binding.rvMissionFanClub, binding.rvMissionPersonal)
            }
        }

        binding.buttonHidePersonal.setOnClickListener {
            if (isExpandPersonal) {
                isExpandPersonal = false

                binding.buttonHidePersonal.setImageResource(R.drawable.expend)
                //binding.rvMissionPersonal.visibility = View.GONE
                //(binding.rvMissionFanClub.layoutParams as LinearLayout.LayoutParams).weight = 90F

                toggleLayout(isExpandPersonal, binding.buttonHidePersonal, binding.rvMissionPersonal, binding.rvMissionFanClub)
            } else {
                isExpandPersonal = true

                binding.buttonHidePersonal.setImageResource(R.drawable.minimize)
                //binding.rvMissionPersonal.visibility = View.VISIBLE
                //(binding.rvMissionFanClub.layoutParams as LinearLayout.LayoutParams).weight = 45F

                toggleLayout(isExpandPersonal, binding.buttonHidePersonal, binding.rvMissionPersonal, binding.rvMissionFanClub)
            }
        }

        binding.buttonSuccessCalendar.setOnClickListener {
            val fragment = FragmentSuccessCalendarLayout()
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.textTabDay.setOnClickListener {
            if (binding.textTabDay.background == null) {
                selectedCycle = ScheduleDTO.CYCLE.DAY
                releaseAllTabButton()
                setTabButton(binding.textTabDay)

                getFanClubSchedule()
                getPersonalSchedule()

                animLeft()
            }
        }
        binding.textTabWeek.setOnClickListener {
            if (binding.textTabWeek.background == null) {
                var oldSelectedCycle = selectedCycle

                selectedCycle = ScheduleDTO.CYCLE.WEEK
                releaseAllTabButton()
                setTabButton(binding.textTabWeek)

                getFanClubSchedule()
                getPersonalSchedule()

                if (oldSelectedCycle < ScheduleDTO.CYCLE.WEEK) {
                    animRight()
                } else {
                    animLeft()
                }
            }
        }
        binding.textTabMonth.setOnClickListener {
            if (binding.textTabMonth.background == null) {
                var oldSelectedCycle = selectedCycle

                selectedCycle = ScheduleDTO.CYCLE.MONTH
                releaseAllTabButton()
                setTabButton(binding.textTabMonth)

                getFanClubSchedule()
                getPersonalSchedule()

                if (oldSelectedCycle < ScheduleDTO.CYCLE.MONTH) {
                    animRight()
                } else {
                    animLeft()
                }
            }
        }
        binding.textTabPeriod.setOnClickListener {
            if (binding.textTabPeriod.background == null) {
                selectedCycle = ScheduleDTO.CYCLE.PERIOD
                releaseAllTabButton()
                setTabButton(binding.textTabPeriod)

                getFanClubSchedule()
                getPersonalSchedule()

                animRight()
            }
        }
    }

    /*private fun getPersonalSchedule() {
        firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            println("?????? ????????? ??????")
            personalSchedules.clear()
            personalMissions.clear()
            if(querySnapshot == null)return@addSnapshotListener
            for(snapshot in querySnapshot){
                var schedule = snapshot.toObject(ScheduleDTO::class.java)!!
                // ?????? ????????? ???????????? ?????? ???????????? ??????
                if (isScheduleVisible(schedule)) {
                    personalSchedules.add(schedule)

                    var mission = DashboardMissionDTO()
                    mission.type = DashboardMissionDTO.TYPE.PERSONAL
                    mission.scheduleDTO = schedule

                    var docName = getProgressDocName(schedule)

                    firestore?.collection("user")
                        ?.document(firebaseAuth?.currentUser?.uid.toString())
                        ?.collection("schedule")?.document(schedule.docName.toString())
                        ?.collection("progress")?.document(docName)?.get()
                        ?.addOnCompleteListener { task ->
                            println("?????? ????????? ??????????????? ??????")
                            if (task.isSuccessful) {
                                var scheduleProgressDTO = ScheduleProgressDTO(docName, 0)
                                if (task.result.exists()) { // document ??????
                                    scheduleProgressDTO =
                                        task.result.toObject(ScheduleProgressDTO::class.java)!!
                                }

                                mission.scheduleProgressDTO = scheduleProgressDTO

                                personalMissions.add(mission)
                            }
                        }
                }
            }

            timer(period = 100)
            {
                if (personalSchedules.size == personalMissions.size) {
                    cancel()
                    activity?.runOnUiThread {
                        setAdapterPersonal()
                    }
                }
            }
        }
    }*/

    /*private fun getFanClubSchedule() {
        if (fanClubDTO == null)
            return

        firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            println("????????? ????????? ??????")
            fanClubSchedules.clear()
            fanClubMissions.clear()
            if(querySnapshot == null)return@addSnapshotListener
            for(snapshot in querySnapshot){
                var schedule = snapshot.toObject(ScheduleDTO::class.java)!!
                // ?????? ????????? ???????????? ?????? ???????????? ??????
                if (isScheduleVisible(schedule)) {
                    fanClubSchedules.add(schedule)

                    var mission = DashboardMissionDTO()
                    mission.type = DashboardMissionDTO.TYPE.FAN_CLUB
                    mission.scheduleDTO = schedule

                    var docName = getProgressDocName(schedule)

                    println("????????? : $schedule")
                    println("????????? : $docName")
                    firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())
                        ?.collection("member")?.document(currentMember?.userUid.toString())
                        ?.collection("schedule")?.document(schedule.docName.toString())
                        ?.collection("progress")?.document(docName)?.get()
                        ?.addOnCompleteListener { task ->
                            println("????????? ????????? ??????????????? ??????")
                            if (task.isSuccessful) {
                                println("?????? ??????? $task")
                                var scheduleProgressDTO = ScheduleProgressDTO(docName, 0)
                                if (task.result.exists()) { // document ??????
                                    scheduleProgressDTO =
                                        task.result.toObject(ScheduleProgressDTO::class.java)!!
                                }

                                mission.scheduleProgressDTO = scheduleProgressDTO

                                println("??????????????? : $scheduleProgressDTO")
                                fanClubMissions.add(mission)
                                println("??????2 ${fanClubMissions.size}")
                            }
                        }
                }
            }
            println("?????? ??????")

            timer(period = 100)
            {
                if (fanClubSchedules.size == fanClubMissions.size) {
                    cancel()
                    activity?.runOnUiThread {
                        setAdapterFanClub()
                        println("?????? ??????22")
                        println("??????3 ${fanClubMissions.size}")
                    }
                }
            }
        }
    }*/

    private fun getPersonalSchedule() {
        recyclerViewPersonal.visibility = View.GONE

        firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnCompleteListener { task ->
            println("?????? ????????? ??????")
            personalSchedules.clear()
            personalMissions.clear()
            if(!task.isSuccessful)return@addOnCompleteListener

            for (document in task.result) {
                var schedule = document.toObject(ScheduleDTO::class.java)!!
                // ?????? ????????? ???????????? ?????? ???????????? ??????
                if (isScheduleVisible(schedule)) {
                    personalSchedules.add(schedule)

                    var mission = DashboardMissionDTO()
                    mission.type = DashboardMissionDTO.TYPE.PERSONAL
                    mission.scheduleDTO = schedule

                    var docName = getProgressDocName(schedule)

                    firestore?.collection("user")
                        ?.document(firebaseAuth?.currentUser?.uid.toString())
                        ?.collection("schedule")?.document(schedule.docName.toString())
                        ?.collection("progress")?.document(docName)?.get()
                        ?.addOnCompleteListener { task ->
                            println("?????? ????????? ??????????????? ??????")
                            if (task.isSuccessful) {
                                var scheduleProgressDTO = ScheduleProgressDTO(docName, 0)
                                if (task.result.exists()) { // document ??????
                                    scheduleProgressDTO =
                                        task.result.toObject(ScheduleProgressDTO::class.java)!!
                                }

                                mission.scheduleProgressDTO = scheduleProgressDTO

                                personalMissions.add(mission)
                            }
                        }
                }
            }

            timer(period = 100)
            {
                if (personalSchedules.size == personalMissions.size) {
                    cancel()
                    activity?.runOnUiThread {
                        setAdapterPersonal()
                    }
                }
            }
        }
    }

    private fun getFanClubSchedule() {
        if (fanClubDTO == null)
            return

        recyclerViewFanClub.visibility = View.GONE

        firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())
            ?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnCompleteListener { task ->
                println("????????? ????????? ??????")
                fanClubSchedules.clear()
                fanClubMissions.clear()
                if(!task.isSuccessful)return@addOnCompleteListener
                for (document in task.result) {
                    var schedule = document.toObject(ScheduleDTO::class.java)!!
                    // ?????? ????????? ???????????? ?????? ???????????? ??????
                    if (isScheduleVisible(schedule)) {
                        fanClubSchedules.add(schedule)

                        var mission = DashboardMissionDTO()
                        mission.type = DashboardMissionDTO.TYPE.FAN_CLUB
                        mission.scheduleDTO = schedule
                        fanClubMissions.add(mission)
                        var position = fanClubMissions.size - 1

                        var docName = getProgressDocName(schedule)

                        println("????????? : $schedule")
                        println("????????? : $docName")
                        firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())
                            ?.collection("member")?.document(currentMember?.userUid.toString())
                            ?.collection("schedule")?.document(schedule.docName.toString())
                            ?.collection("progress")?.document(docName)?.get()
                            ?.addOnCompleteListener { task ->
                                println("????????? ????????? ??????????????? ??????")
                                if (task.isSuccessful) {
                                    println("?????? ??????? $task")
                                    var scheduleProgressDTO = ScheduleProgressDTO(docName, 0)
                                    if (task.result.exists()) { // document ??????
                                        scheduleProgressDTO =
                                            task.result.toObject(ScheduleProgressDTO::class.java)!!
                                    }

                                    mission.scheduleProgressDTO = scheduleProgressDTO

                                    println("??????????????? : $scheduleProgressDTO")
                                    //fanClubMissions.add(mission)
                                    fanClubMissions[position].scheduleProgressDTO = scheduleProgressDTO
                                    println("??????2 ${fanClubMissions.size}")
                                }
                            }
                    }
            }
            println("????????? ????????? $fanClubSchedules")
                println("????????? ?????? $fanClubMissions")

            timer(period = 100)
            {
                if (fanClubSchedules.size == fanClubMissions.size) {
                    var isNull = false
                    for (i in fanClubMissions) {
                        if (i.scheduleProgressDTO == null) {
                            isNull = true
                            break
                        }
                    }
                    if (!isNull) {

                        cancel()
                        activity?.runOnUiThread {
                            setAdapterFanClub()
                            println("?????? ??????22")
                            println("??????3 ${fanClubMissions.size}")
                        }
                    }
                }
            }
        }
    }

    private fun setAdapterPersonal() {
        recyclerViewPersonalAdapter = RecyclerViewAdapterMission(personalMissions, this)
        recyclerViewPersonal.adapter = recyclerViewPersonalAdapter
        recyclerViewPersonal.visibility = View.VISIBLE
    }

    private fun setAdapterFanClub() {
        recyclerViewFanClubAdapter = RecyclerViewAdapterMission(fanClubMissions, this)
        recyclerViewFanClub.adapter = recyclerViewFanClubAdapter
        recyclerViewFanClub.visibility = View.VISIBLE
    }

    // ?????? ????????? ???????????? ?????? ??????????????? ??????
    private fun isScheduleVisible(schedule: ScheduleDTO) : Boolean {
        if (selectedCycle != schedule.cycle) {
            return false
        }

        val calStart = Calendar.getInstance()
        calStart.time = schedule.startDate
        calStart.set(Calendar.HOUR, 0)
        calStart.set(Calendar.MINUTE, 0)
        calStart.set(Calendar.SECOND, 0)

        val calEnd = Calendar.getInstance()
        calEnd.time = schedule.endDate
        calEnd.set(Calendar.HOUR, 23)
        calEnd.set(Calendar.MINUTE, 59)
        calEnd.set(Calendar.SECOND, 59)

        var date = Date()

        return date >= calStart.time && date <= calEnd.time
    }

    private fun getProgressDocName(schedule: ScheduleDTO) : String {
        var docName = ""

        when (schedule.cycle) {
            ScheduleDTO.CYCLE.DAY -> docName = SimpleDateFormat("yyyyMMdd").format(Date())
            ScheduleDTO.CYCLE.WEEK -> {
                var successCalendarWeek = SuccessCalendarWeek(Date())
                successCalendarWeek.initBaseCalendar()
                var week = successCalendarWeek.getCurrentWeek()
                if (week != null) {
                    docName = "${SimpleDateFormat("yyyyMMdd").format(week.startDate)}${SimpleDateFormat("yyyyMMdd").format(week.endDate)}"
                }
            }
            ScheduleDTO.CYCLE.MONTH -> docName = SimpleDateFormat("yyyyMM").format(Date())
            ScheduleDTO.CYCLE.PERIOD -> docName = "${SimpleDateFormat("yyMMdd").format(schedule.startDate)}${SimpleDateFormat("yyMMdd").format(schedule.endDate)}"
        }
        return docName
    }

    private fun setTabButton(textView: TextView) {
        textView.background = getDrawable(requireContext(), btn_round)
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun releaseTabButton(textView: TextView) {
        textView.background = null
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun releaseAllTabButton() {
        releaseTabButton(binding.textTabDay)
        releaseTabButton(binding.textTabWeek)
        releaseTabButton(binding.textTabMonth)
        releaseTabButton(binding.textTabPeriod)
    }

    private fun animLeft() {
        val translateLeft = AnimationUtils.loadAnimation(context, R.anim.translate_left)
        binding.layoutSchedule.startAnimation(translateLeft)
    }

    private fun animRight() {
        val translateRight = AnimationUtils.loadAnimation(context, R.anim.translate_right)
        binding.layoutSchedule.startAnimation(translateRight)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentDashboardMission.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: FanClubDTO?, param2: MemberDTO?) =
            FragmentDashboardMission().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: DashboardMissionDTO, position: Int) {
        val dialog = MissionDialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.dashboardMissionDTO = item
        dialog.show()

        dialog.button_mission_cancel.setOnClickListener { // No
            dialog.dismiss()
        }

        dialog.button_mission_ok.setOnClickListener { // Ok
            item.scheduleProgressDTO?.count = dialog.missionCount
            when (item.type) {
                DashboardMissionDTO.TYPE.PERSONAL -> {
                    firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.document(item.scheduleDTO?.docName.toString())?.collection("progress")?.document(item.scheduleProgressDTO?.docName.toString())?.set(item.scheduleProgressDTO!!)?.addOnCompleteListener {
                        recyclerViewPersonalAdapter.notifyItemChanged(position)
                        Toast.makeText(activity,"?????? ?????? ??????.", Toast.LENGTH_SHORT).show()
                    }
                }
                DashboardMissionDTO.TYPE.FAN_CLUB -> {
                    firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())
                        ?.collection("member")?.document(currentMember?.userUid.toString())
                        ?.collection("schedule")?.document(item.scheduleDTO?.docName.toString())
                        ?.collection("progress")?.document(item.scheduleProgressDTO?.docName.toString())?.set(item.scheduleProgressDTO!!)?.addOnCompleteListener {
                        recyclerViewFanClubAdapter.notifyItemChanged(position)
                        Toast.makeText(activity,"????????? ?????? ?????? ??????.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            dialog.dismiss()
        }
    }
}