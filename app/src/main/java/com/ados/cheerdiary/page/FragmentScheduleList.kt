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
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentScheduleListBinding
import com.ados.cheerdiary.model.ScheduleDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentScheduleList.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentScheduleList : Fragment(), OnScheduleItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentScheduleListBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterSchedule

    private var schedules : ArrayList<ScheduleDTO> = arrayListOf()
    private var selectedSchedule: ScheduleDTO? = null

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
        _binding = FragmentScheduleListBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerView = rootView.findViewById(R.id.rv_schedule!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 메뉴는 기본 숨김
        binding.layoutMenu.visibility = View.GONE

        firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            println("스케줄 호출")
            schedules.clear()
            for (document in result) {
                var schedule = document.toObject(ScheduleDTO::class.java)!!
                schedules.add(schedule)
            }
            recyclerViewAdapter = RecyclerViewAdapterSchedule(schedules, this)
            recyclerView.adapter = recyclerViewAdapter
        }?.addOnFailureListener { exception ->

        }




        val swipeHelperCallback = SwipeHelperCallback()
        val itemTouchHelper = ItemTouchHelper(swipeHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAddSchedule.setOnClickListener {
            val fragment = FragmentScheduleAdd()
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.buttonModify.setOnClickListener {
            val fragment = FragmentScheduleAdd()
            fragment.scheduleDTO = selectedSchedule!!
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
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
         * @return A new instance of fragment FragmentScheduleList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentScheduleList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: ScheduleDTO, position: Int) {
        if (recyclerViewAdapter?.selectItem(position)) { // 선택 일 경우 메뉴 표시 및 레이아웃 어둡게
            val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
            binding.layoutMenu.visibility = View.VISIBLE
            binding.layoutMenu.startAnimation(translateUp)
            //recyclerView.smoothSnapToPosition(position)
            selectedSchedule = item
        } else { // 해제 일 경우 메뉴 숨김 및 레이아웃 밝게
            val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
            binding.layoutMenu.visibility = View.GONE
            binding.layoutMenu.startAnimation(translateDown)
            //recyclerView.smoothSnapToPosition(position)
        }

    }

    fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = snapMode
            override fun getHorizontalSnapPreference(): Int = snapMode
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }
}