package com.ados.cheerdiary.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.cheerdiary.MainActivity
import com.ados.cheerdiary.R
import com.ados.cheerdiary.databinding.FragmentFanClubInfoBinding
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
 * Use the [FragmentFanClubInfo.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubInfo : Fragment(), OnFanClubMemberItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFanClubInfoBinding? = null
    private val binding get() = _binding!!

    private var firestore : FirebaseFirestore? = null

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClubMember

    private var fanClubDTO: FanClubDTO? = null
    private var members : ArrayList<MemberDTO> = arrayListOf()

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
        _binding = FragmentFanClubInfoBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firestore = FirebaseFirestore.getInstance()

        recyclerView = rootView.findViewById(R.id.rv_fan_club_member!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 메뉴는 기본 숨김
        binding.layoutMenu.visibility = View.GONE

        val user = (activity as MainActivity?)?.getUser()
        firestore?.collection("fanClub")?.document(user?.fanClubId!!)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result.exists()) { // document 있음
                    fanClubDTO = task.result.toObject(FanClubDTO::class.java)!!

                    firestore?.collection("fanClub")?.document(user?.fanClubId!!)?.collection("member")?.get()?.addOnCompleteListener { task ->
                        members.clear()
                        if(task.isSuccessful) {
                            for (document in task.result) {
                                var member = document.toObject(MemberDTO::class.java)!!
                                if (member.position != MemberDTO.POSITION.GUEST) {
                                    members.add(member)
                                }
                            }

                            recyclerViewAdapter = RecyclerViewAdapterFanClubMember(members, this)
                            recyclerView.adapter = recyclerViewAdapter
                        }
                    }
                } else {
                    // 팬클럽 정보 가져오기 실패
                }
            }
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubInfo.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentFanClubInfo().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: MemberDTO, position: Int) {
        if (recyclerViewAdapter?.selectItem(position)) { // 선택 일 경우 메뉴 표시 및 레이아웃 어둡게
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
}