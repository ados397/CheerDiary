package com.ados.cheerdiary.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentFanClubScheduleBinding
import com.ados.cheerdiary.model.FanClubDTO
import com.ados.cheerdiary.model.MemberDTO
import com.ados.cheerdiary.model.ScheduleDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubSchedule.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubSchedule : Fragment(), OnScheduleItemClickListener, OnStartDragListener {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentFanClubScheduleBinding? = null
    private val binding get() = _binding!!

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterSchedule
    lateinit var itemTouchHelper : ItemTouchHelper

    private var firestore : FirebaseFirestore? = null

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    private var schedules : ArrayList<ScheduleDTO> = arrayListOf()
    private var schedulesBackup : ArrayList<ScheduleDTO> = arrayListOf()
    private var selectedSchedule: ScheduleDTO? = null

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
        _binding = FragmentFanClubScheduleBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firestore = FirebaseFirestore.getInstance()

        recyclerView = rootView.findViewById(R.id.rv_fan_club_schedule!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ????????? ?????? ??????
        binding.layoutMenu.visibility = View.GONE
        binding.layoutMenuReorder.visibility = View.GONE

        firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            println("????????? ??????")
            schedules.clear()
            for (document in result) {
                var schedule = document.toObject(ScheduleDTO::class.java)!!
                schedules.add(schedule)
            }
            setAdapter()
        }?.addOnFailureListener { exception ->

        }

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAddSchedule.setOnClickListener {
            val fragment = FragmentScheduleAdd.newInstance(fanClubDTO, currentMember)
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.viewpager, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.buttonReorder.setOnClickListener {
            visibleReorder()

            // ?????? ???????????? ?????? ????????? ?????? ????????? ??????
            schedulesBackup.clear()
            schedulesBackup.addAll(schedules)

            recyclerViewAdapter.showReorderIcon = true
            recyclerViewAdapter.notifyDataSetChanged()
        }

        binding.buttonModify.setOnClickListener {
            val fragment = FragmentScheduleAdd()
            fragment.scheduleDTO = selectedSchedule!!
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.viewpager, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.buttonDelete.setOnClickListener {

        }

        binding.buttonOk.setOnClickListener {
            disableReorder()

            recyclerViewAdapter.showReorderIcon = false
            recyclerViewAdapter.notifyDataSetChanged()


            for (schedule in schedules) {
                println("????????? ?????? $schedule")
                firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("schedule")?.document(schedule.docName.toString())?.set(schedule)?.addOnCompleteListener {

                }
            }
        }

        binding.buttonCancel.setOnClickListener {
            disableReorder()

            // ?????? ????????? ??????
            schedules.clear()
            schedules.addAll(schedulesBackup)
            setAdapter()
        }
    }

    private fun setAdapter() {
        recyclerViewAdapter = RecyclerViewAdapterSchedule(schedules, this, this)
        recyclerView.adapter = recyclerViewAdapter

        val swipeHelperCallback = SwipeHelperCallback(recyclerViewAdapter)
        itemTouchHelper = ItemTouchHelper(swipeHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun visibleReorder() {
        binding.buttonAddSchedule.visibility = View.GONE
        binding.buttonReorder.visibility = View.GONE
        binding.layoutMenuModify.visibility = View.GONE

        val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
        binding.layoutMenu.visibility = View.VISIBLE
        binding.layoutMenuReorder.visibility = View.VISIBLE
        binding.layoutMenu.startAnimation(translateUp)
    }

    private fun disableReorder() {
        binding.buttonAddSchedule.visibility = View.VISIBLE
        binding.buttonReorder.visibility = View.VISIBLE
        binding.layoutMenuModify.visibility = View.VISIBLE

        val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
        binding.layoutMenu.visibility = View.GONE
        binding.layoutMenuReorder.visibility = View.GONE
        binding.layoutMenu.startAnimation(translateDown)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubSchedule.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: FanClubDTO?, param2: MemberDTO?) =
            FragmentFanClubSchedule().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: ScheduleDTO, position: Int) {
        if (recyclerViewAdapter?.selectItem(position)) { // ?????? ??? ?????? ?????? ?????? ??? ???????????? ?????????
            val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
            binding.layoutMenu.visibility = View.VISIBLE
            binding.layoutMenuModify.visibility = View.VISIBLE
            binding.layoutMenu.startAnimation(translateUp)
            //recyclerView.smoothSnapToPosition(position)
            selectedSchedule = item
        } else { // ?????? ??? ?????? ?????? ?????? ??? ???????????? ??????
            val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
            binding.layoutMenu.visibility = View.GONE
            binding.layoutMenu.startAnimation(translateDown)
            //recyclerView.smoothSnapToPosition(position)
        }
    }

    override fun onStartDrag(holder: RecyclerViewAdapterSchedule.ViewHolder) {
        itemTouchHelper.startDrag(holder)
    }
}