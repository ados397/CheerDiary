package com.ados.cheerdiary.page

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.MainActivity
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentFanClubJoinBinding
import com.ados.cheerdiary.model.FanClubDTO
import com.ados.cheerdiary.model.MemberDTO
import com.ados.cheerdiary.model.UserDTO
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubJoin.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubJoin : Fragment(), OnFanClubItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFanClubJoinBinding? = null
    private val binding get() = _binding!!

    private var firestore : FirebaseFirestore? = null

    private lateinit var callback: OnBackPressedCallback

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClub

    private var fanClubs : ArrayList<FanClubDTO> = arrayListOf()

    private var selectedFanClub: FanClubDTO? = null
    private var selectedPosition: Int? = 0

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
        _binding = FragmentFanClubJoinBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firestore = FirebaseFirestore.getInstance()

        recyclerView = rootView.findViewById(R.id.rv_fan_club!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 메뉴는 기본 숨김
        binding.layoutMenu.visibility = View.GONE

        searchFanClub()




        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.buttonSearch.setOnClickListener {
            val clubName = binding.searchView.query.toString()
            Toast.makeText(activity, "검색어 $clubName", Toast.LENGTH_SHORT).show()
            searchFanClub()
        }

        binding.buttonCancel.setOnClickListener {
            selectRecyclerView()
        }

        binding.buttonRequest.setOnClickListener {
            val user = (activity as MainActivity?)?.getUser()
            firestore?.collection("fanClub")?.document(selectedFanClub?.docName.toString())?.collection("member")?.document(user?.uid.toString())?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result.exists()) { // document 있음
                        Toast.makeText(activity, "이미 가입 요청을 한 팬클럽 입니다!", Toast.LENGTH_SHORT).show()
                    } else { // document 없으면 회원 가입 페이지로 이동
                        val member = MemberDTO(false, user?.uid, user?.nickname, 0, MemberDTO.POSITION.GUEST, false)
                        firestore?.collection("fanClub")?.document(selectedFanClub?.docName.toString())?.collection("member")?.document(user?.uid.toString())?.set(member)?.addOnCompleteListener {
                            Toast.makeText(activity, "팬클럽 가입 요청 완료!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                selectRecyclerView()
            }
        }
    }

    private fun searchFanClub() {
        var query = binding.searchView.query
        firestore?.collection("fanClub")?.get()?.addOnCompleteListener { task ->
            fanClubs.clear()
            if (task.isSuccessful) {
                for (document in task.result) {
                    var fanClub = document.toObject(FanClubDTO::class.java)!!
                    if (!query.isNullOrEmpty()) {
                        if (fanClub.name!!.contains(query)) {
                            fanClubs.add(fanClub)
                        }
                    } else {
                        fanClubs.add(fanClub)
                    }
                }
                recyclerViewAdapter = RecyclerViewAdapterFanClub(fanClubs, this)
                recyclerView.adapter = recyclerViewAdapter
            }
        }
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
        val fragment = FragmentFanClubInitalize()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubJoin.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentFanClubJoin().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun selectRecyclerView() {
        if (recyclerViewAdapter?.selectItem(selectedPosition!!)) { // 선택 일 경우 메뉴 표시 및 레이아웃 어둡게
            val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
            binding.layoutMenu.visibility = View.VISIBLE
            binding.layoutMenu.startAnimation(translateUp)
            //recyclerView.smoothSnapToPosition(position)
        } else { // 해제 일 경우 메뉴 숨김 및 레이아웃 밝게
            val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
            binding.layoutMenu.visibility = View.GONE
            binding.layoutMenu.startAnimation(translateDown)
            //recyclerView.smoothSnapToPosition(position)
        }
    }

    override fun onItemClick(item: FanClubDTO, position: Int) {
        selectedFanClub = item
        selectedPosition = position
        selectRecyclerView()
    }
}