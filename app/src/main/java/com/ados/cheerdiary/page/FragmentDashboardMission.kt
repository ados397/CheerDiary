package com.ados.cheerdiary.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentDashboardMissionBinding
import com.ados.cheerdiary.model.ScheduleDTO
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.ados.cheerdiary.R.drawable.btn_round
import com.ados.cheerdiary.SuccessCalendarWeek
import com.ados.cheerdiary.dialog.MissionDialog
import com.ados.cheerdiary.model.DashboardMissionDTO
import com.ados.cheerdiary.model.ScheduleProgressDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.select_app_dialog.*
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
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentDashboardMissionBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null

    lateinit var recyclerViewClub : RecyclerView
    lateinit var recyclerViewClubAdapter : RecyclerViewAdapterMission
    lateinit var recyclerViewPersonal : RecyclerView
    lateinit var recyclerViewPersonalAdapter : RecyclerViewAdapterMission

    private var personalSchedules : ArrayList<ScheduleDTO> = arrayListOf()
    private var personalMissions : ArrayList<DashboardMissionDTO> = arrayListOf()
    private var clubSchedules : ArrayList<ScheduleDTO> = arrayListOf()
    private var clubMissions : ArrayList<DashboardMissionDTO> = arrayListOf()

    private var isExpandClub: Boolean = true
    private var isExpandPersonal: Boolean = true

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
        _binding = FragmentDashboardMissionBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerViewClub = rootView.findViewById(R.id.rv_mission_club!!)as RecyclerView
        recyclerViewClub.layoutManager = LinearLayoutManager(requireContext())

        recyclerViewPersonal = rootView.findViewById(R.id.rv_mission_personal!!)as RecyclerView
        recyclerViewPersonal.layoutManager = LinearLayoutManager(requireContext())


        recyclerViewClubAdapter = RecyclerViewAdapterMission(clubMissions, this)
        recyclerViewClub.adapter = recyclerViewClubAdapter

        firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            personalSchedules.clear()
            personalMissions.clear()
            if(querySnapshot == null)return@addSnapshotListener
            for(snapshot in querySnapshot){
                var schedule = snapshot.toObject(ScheduleDTO::class.java)!!
                // 현재 시간이 기간내에 속한 스케줄만 표시
                if (isScheduleVisible(schedule)) {
                    personalSchedules.add(schedule)

                    var mission = DashboardMissionDTO()
                    mission.scheduleDTO = schedule

                    var docName = getProgressDocName(schedule)

                    println("스케줄 : $schedule")
                    println("독네임 : $docName")
                    firestore?.collection("user")
                        ?.document(firebaseAuth?.currentUser?.uid.toString())
                        ?.collection("schedule")?.document(schedule.docName.toString())
                        ?.collection("progress")?.document(docName)?.get()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                println("조회 성공? $task")
                                var scheduleProgressDTO = ScheduleProgressDTO(docName, 0)
                                if (task.result.exists()) { // document 있음
                                    scheduleProgressDTO =
                                        task.result.toObject(ScheduleProgressDTO::class.java)!!
                                }

                                mission.scheduleProgressDTO = scheduleProgressDTO

                                println("프로그레스 : $scheduleProgressDTO")
                                personalMissions.add(mission)
                                println("호출2 ${personalMissions.size}")
                            }
                        }
                }
            }
            println("호출 순서")

            timer(period = 100)
            {
                if (personalSchedules.size == personalMissions.size) {
                    cancel()
                    activity?.runOnUiThread {
                        setAdapter()
                        println("호출 순서22")
                        println("호출3 ${personalMissions.size}")
                    }
                }
            }
        }
        /*firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            personalSchedules.clear()
            personalMissions.clear()
            println("호출1 ${personalMissions.size}")
            for (document in result) {
                var schedule = document.toObject(ScheduleDTO::class.java)!!
                personalSchedules.add(schedule)

                var mission = DashboardMissionDTO()
                mission.scheduleDTO = schedule

                var docName = getProgressDocName(schedule)

                println("스케줄 : $schedule")
                println("독네임 : $docName")
                firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.document(schedule.docName.toString())?.collection("progress")?.document(docName)?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("조회 성공? $task")
                        var scheduleProgressDTO = ScheduleProgressDTO(docName, 0)
                        if (task.result.exists()) { // document 있음
                            scheduleProgressDTO = task.result.toObject(ScheduleProgressDTO::class.java)!!
                        }

                        mission.scheduleProgressDTO = scheduleProgressDTO

                        println("프로그레스 : $scheduleProgressDTO")
                        personalMissions.add(mission)
                        println("호출2 ${personalMissions.size}")
                    }
                }
            }
            println("호출 순서")

            timer(period = 100)
            {
                if (personalSchedules.size == personalMissions.size) {
                    cancel()
                    activity?.runOnUiThread {
                        setAdapter()
                        println("호출 순서22")
                        println("호출3 ${personalMissions.size}")
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonHideClub.setOnClickListener {
            if (isExpandClub) {
                isExpandClub = false


                //binding.layoutClub.startAnimation(translateUp)
                //binding.layoutPersonal.startAnimation(translateUp)

                binding.buttonHideClub.setImageResource(R.drawable.expend)
                binding.rvMissionClub.visibility = View.GONE
                (binding.rvMissionPersonal.layoutParams as LinearLayout.LayoutParams).weight = 90F

                //val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
                //binding.rvMissionClub.startAnimation(translateUp)

                //(binding.layoutClub.layoutParams as LinearLayout.LayoutParams).weight = 0.5F
                //(binding.layoutTitleClub.layoutParams as LinearLayout.LayoutParams).weight = 10F
                //(binding.layoutPersonal.layoutParams as LinearLayout.LayoutParams).weight = 9.5F



            } else {
                isExpandClub = true

                //binding.layoutClub.startAnimation(translateDown)
                //binding.layoutPersonal.startAnimation(translateDown)

                binding.buttonHideClub.setImageResource(R.drawable.minimize)
                binding.rvMissionClub.visibility = View.VISIBLE
                (binding.rvMissionPersonal.layoutParams as LinearLayout.LayoutParams).weight = 45F

                //val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
                //binding.rvMissionClub.startAnimation(translateDown)

                //(binding.layoutClub.layoutParams as LinearLayout.LayoutParams).weight = 5F
                //(binding.layoutTitleClub.layoutParams as LinearLayout.LayoutParams).weight = 1F
                //(binding.layoutPersonal.layoutParams as LinearLayout.LayoutParams).weight = 5F
            }
        }

        binding.buttonHidePersonal.setOnClickListener {
            if (isExpandPersonal) {
                isExpandPersonal = false

                binding.buttonHidePersonal.setImageResource(R.drawable.expend)
                binding.rvMissionPersonal.visibility = View.GONE
                (binding.rvMissionClub.layoutParams as LinearLayout.LayoutParams).weight = 90F

                //(binding.layoutPersonal.layoutParams as LinearLayout.LayoutParams).weight = 0.5F
                //(binding.layoutTitlePersonal.layoutParams as LinearLayout.LayoutParams).weight = 10F
                //(binding.layoutClub.layoutParams as LinearLayout.LayoutParams).weight = 9.5F
            } else {
                isExpandPersonal = true

                binding.buttonHidePersonal.setImageResource(R.drawable.minimize)
                binding.rvMissionPersonal.visibility = View.VISIBLE
                (binding.rvMissionClub.layoutParams as LinearLayout.LayoutParams).weight = 45F

                //(binding.layoutPersonal.layoutParams as LinearLayout.LayoutParams).weight = 5F
                //(binding.layoutTitlePersonal.layoutParams as LinearLayout.LayoutParams).weight = 1F
                //(binding.layoutClub.layoutParams as LinearLayout.LayoutParams).weight = 5F
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
            setTabButton(binding.textTabDay)
            releaseTabButton(binding.textTabWeek)
            releaseTabButton(binding.textTabMonth)
            releaseTabButton(binding.textTabPeriod)
        }
        binding.textTabWeek.setOnClickListener {
            setTabButton(binding.textTabWeek)
            releaseTabButton(binding.textTabDay)
            releaseTabButton(binding.textTabMonth)
            releaseTabButton(binding.textTabPeriod)
        }
        binding.textTabMonth.setOnClickListener {
            setTabButton(binding.textTabMonth)
            releaseTabButton(binding.textTabDay)
            releaseTabButton(binding.textTabWeek)
            releaseTabButton(binding.textTabPeriod)
        }
        binding.textTabPeriod.setOnClickListener {
            setTabButton(binding.textTabPeriod)
            releaseTabButton(binding.textTabDay)
            releaseTabButton(binding.textTabWeek)
            releaseTabButton(binding.textTabMonth)
        }
    }

    private fun setAdapter() {
        recyclerViewPersonalAdapter = RecyclerViewAdapterMission(personalMissions, this)
        recyclerViewPersonal.adapter = recyclerViewPersonalAdapter
    }

    // 현재 시간이 기간내에 속한 스케줄인지 확인
    private fun isScheduleVisible(schedule: ScheduleDTO) : Boolean {
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
        fun newInstance(param1: String, param2: String) =
            FragmentDashboardMission().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: DashboardMissionDTO, position: Int) {
        val dialog = MissionDialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.dashboardMissionDTO = item
        dialog.show()

        dialog.button_cancel.setOnClickListener { // No
            dialog.dismiss()
        }

        dialog.button_ok.setOnClickListener { // Ok
            item.scheduleProgressDTO?.count = dialog.missionCount
            firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.document(item.scheduleDTO?.docName.toString())?.collection("progress")?.document(item.scheduleProgressDTO?.docName.toString())?.set(item.scheduleProgressDTO!!)?.addOnCompleteListener {
                recyclerViewPersonalAdapter.notifyItemChanged(position)
                Toast.makeText(activity,"적용 완료.", Toast.LENGTH_SHORT).show()
                println("미션 적용 완료 ${personalMissions.size}")
            }

            dialog.dismiss()
        }
    }
}